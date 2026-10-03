package com.telecom.access.security.rbac;

import com.telecom.access.entity.EmploymentType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * The authenticated user: authorities for RBAC (expanded roles + permissions)
 * plus subject attributes for ABAC (circle, employment type).
 */
public record TelecomUserPrincipal(
        String username,
        String password,
        boolean enabled,
        Set<String> roles,                     // reachable roles, without the ROLE_ prefix
        String circle,
        EmploymentType employmentType,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    public boolean hasRole(String role) { return roles.contains(role); }
    public boolean isStaff() { return roles.contains("AGENT"); }      // every staff role reaches AGENT
}
