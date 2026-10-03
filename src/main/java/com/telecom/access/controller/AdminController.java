package com.telecom.access.controller;

import com.telecom.access.dto.*;
import com.telecom.access.service.AccessAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/** Dynamic RBAC/ABAC administration. URL rule: ROLE_ADMIN; method rules: specific permissions. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AccessAdminService service;

    public AdminController(AccessAdminService service) {
        this.service = service;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('role:manage')")
    public List<RoleView> roles() {
        return service.listRoles();
    }

    @PutMapping("/roles/{role}/permissions/{permission}")
    @PreAuthorize("hasAuthority('role:manage')")
    public RoleView grant(@PathVariable String role, @PathVariable String permission, Authentication auth) {
        return service.grantPermission(role, permission, auth.getName());
    }

    @DeleteMapping("/roles/{role}/permissions/{permission}")
    @PreAuthorize("hasAuthority('role:manage')")
    public RoleView revoke(@PathVariable String role, @PathVariable String permission, Authentication auth) {
        return service.revokePermission(role, permission, auth.getName());
    }

    /** {parent} inherits {role}: PUT /roles/AGENT/parent/SUPERVISOR -> ROLE_SUPERVISOR > ROLE_AGENT */
    @PutMapping("/roles/{role}/parent/{parent}")
    @PreAuthorize("hasAuthority('role:manage')")
    public RoleView setParent(@PathVariable String role, @PathVariable String parent, Authentication auth) {
        return service.setParent(role, parent, auth.getName());
    }

    @PutMapping("/users/{username}/roles")
    @PreAuthorize("hasAuthority('user:manage') and #username != authentication.name")   // no self-escalation
    public Set<String> assignRoles(@PathVariable String username, @Valid @RequestBody UserRolesRequest request,
                                   Authentication auth) {
        return service.assignRoles(username, request.roles(), auth.getName());
    }

    @PatchMapping("/users/{username}/attributes")
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<Void> attributes(@PathVariable String username, @RequestBody UserAttributesRequest request,
                                           Authentication auth) {
        service.updateAttributes(username, request, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/policies/{key}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<Void> setting(@PathVariable String key, @Valid @RequestBody SettingRequest request,
                                        Authentication auth) {
        service.updateSetting(key, request.value(), auth.getName());
        return ResponseEntity.noContent().build();
    }
}
