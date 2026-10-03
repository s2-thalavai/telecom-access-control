package com.telecom.access.security.rbac;

import com.telecom.access.entity.AppUser;
import com.telecom.access.entity.Role;
import com.telecom.access.repository.AppUserRepository;
import com.telecom.access.repository.RoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads a user and turns RBAC data into authorities:
 *   1. direct roles             ROLE_SUPERVISOR
 *   2. + implied roles           ROLE_AGENT          (via the role hierarchy)
 *   3. + permissions of ALL reachable roles: recharge:refund, subscriber:read, ...
 * Because HTTP Basic re-authenticates every request, role/permission changes apply immediately.
 */
@Service
public class TelecomUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;
    private final RoleRepository roles;
    private final DynamicRoleHierarchy hierarchy;

    public TelecomUserDetailsService(AppUserRepository users, RoleRepository roles, DynamicRoleHierarchy hierarchy) {
        this.users = users;
        this.roles = roles;
        this.hierarchy = hierarchy;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        List<GrantedAuthority> direct = user.getRoles().stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .toList();
        Set<String> reachableRoles = hierarchy.getReachableGrantedAuthorities(direct).stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toCollection(TreeSet::new));

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String roleName : reachableRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            roles.findById(roleName).map(Role::getPermissions).orElse(Set.of())
                    .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        return new TelecomUserPrincipal(user.getUsername(), user.getPasswordHash(), user.isEnabled(),
                reachableRoles, user.getCircle(), user.getEmploymentType(), authorities);
    }
}
