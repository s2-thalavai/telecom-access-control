package com.telecom.access.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * RBAC role. "parent" builds the hierarchy: the parent INHERITS everything this role can do
 * (ADMIN -> SUPERVISOR -> AGENT). Permissions are fine-grained strings like "plan:change".
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @Column(length = 30)
    private String name;

    /** The role directly above this one (inherits this role). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_name")
    private Role parent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_name"))
    @Column(name = "permission", length = 50)
    private Set<String> permissions = new HashSet<>();

    protected Role() { }

    public Role(String name, Role parent, Set<String> permissions) {
        this.name = name;
        this.parent = parent;
        this.permissions = new HashSet<>(permissions);
    }

    public String getName() { return name; }
    public Role getParent() { return parent; }
    public void setParent(Role parent) { this.parent = parent; }
    public Set<String> getPermissions() { return permissions; }
}
