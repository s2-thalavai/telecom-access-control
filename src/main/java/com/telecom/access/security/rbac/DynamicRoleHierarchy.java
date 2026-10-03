package com.telecom.access.security.rbac;

import com.telecom.access.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Role hierarchy loaded from the DB (roles.parent_name) instead of being hard-coded,
 * so an admin can reshape it at runtime. Produces e.g.
 *   ROLE_ADMIN > ROLE_SUPERVISOR
 *   ROLE_SUPERVISOR > ROLE_AGENT
 */
@Component
public class DynamicRoleHierarchy implements RoleHierarchy {

    private static final Logger log = LoggerFactory.getLogger(DynamicRoleHierarchy.class);

    private final RoleRepository roles;
    private volatile RoleHierarchy delegate = RoleHierarchyImpl.fromHierarchy("");

    public DynamicRoleHierarchy(RoleRepository roles) {
        this.roles = roles;
    }

    @Transactional(readOnly = true)
    public synchronized void refresh() {
        String hierarchy = roles.findAll().stream()
                .filter(r -> r.getParent() != null)
                .map(r -> "ROLE_" + r.getParent().getName() + " > ROLE_" + r.getName())
                .collect(Collectors.joining("\n"));
        delegate = RoleHierarchyImpl.fromHierarchy(hierarchy);
        log.info("Role hierarchy loaded:\n{}", hierarchy);
    }

    @Override
    public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        return delegate.getReachableGrantedAuthorities(authorities);
    }

    /** True if {@code role} is the same as, or implied by, {@code ancestorRole}. */
    public boolean implies(String ancestorRole, String role) {
        return getReachableGrantedAuthorities(List.of(new SimpleGrantedAuthority("ROLE_" + ancestorRole)))
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
