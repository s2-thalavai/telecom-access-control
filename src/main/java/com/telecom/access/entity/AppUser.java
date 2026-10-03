package com.telecom.access.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/** A user carries RBAC data (roles) AND ABAC subject attributes (circle, employment type). */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private boolean enabled = true;

    /** ABAC attribute: telecom circle (region) the user works in. "ALL" for head office. */
    @Column(length = 5)
    private String circle;

    /** ABAC attribute. */
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private EmploymentType employmentType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    private Set<Role> roles = new HashSet<>();

    protected AppUser() { }

    public AppUser(String username, String passwordHash, String circle, EmploymentType type, Set<Role> roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.circle = circle;
        this.employmentType = type;
        this.roles = new HashSet<>(roles);
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEnabled() { return enabled; }
    public String getCircle() { return circle; }
    public void setCircle(String circle) { this.circle = circle; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
