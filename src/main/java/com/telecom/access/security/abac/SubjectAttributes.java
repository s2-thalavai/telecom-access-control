package com.telecom.access.security.abac;

import com.telecom.access.entity.EmploymentType;

import java.util.Set;

/** WHO is asking. */
public record SubjectAttributes(String username, Set<String> roles, Set<String> permissions,
                                String circle, EmploymentType employmentType) {
    public boolean hasRole(String role) { return roles.contains(role); }
    public boolean isStaff() { return roles.contains("AGENT"); }
    public boolean isAdmin() { return roles.contains("ADMIN"); }
}
