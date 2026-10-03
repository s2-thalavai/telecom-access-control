package com.telecom.access.dto;

import com.telecom.access.entity.Role;
import java.util.Set;
import java.util.TreeSet;

public record RoleView(String name, String inheritedBy, Set<String> permissions) {
    public static RoleView from(Role r) {
        return new RoleView(r.getName(), r.getParent() == null ? null : r.getParent().getName(),
                new TreeSet<>(r.getPermissions()));
    }
}
