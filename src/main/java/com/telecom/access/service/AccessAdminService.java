package com.telecom.access.service;

import com.telecom.access.dto.RoleView;
import com.telecom.access.dto.UserAttributesRequest;
import com.telecom.access.entity.AppUser;
import com.telecom.access.entity.Role;
import com.telecom.access.exception.NotFoundException;
import com.telecom.access.repository.AppUserRepository;
import com.telecom.access.repository.RoleRepository;
import com.telecom.access.security.abac.PolicySettingsService;
import com.telecom.access.security.rbac.DynamicRoleHierarchy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Runtime management of RBAC (roles, permissions, hierarchy, user-role assignment)
 * and ABAC (user attributes, policy settings). Every change is audited and takes
 * effect on the next request because principals are rebuilt per request.
 */
@Service
@Transactional
public class AccessAdminService {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");
    private static final Pattern PERMISSION = Pattern.compile("[a-z]+:[a-z:]+");

    private final RoleRepository roles;
    private final AppUserRepository users;
    private final DynamicRoleHierarchy hierarchy;
    private final PolicySettingsService settings;

    public AccessAdminService(RoleRepository roles, AppUserRepository users,
                              DynamicRoleHierarchy hierarchy, PolicySettingsService settings) {
        this.roles = roles;
        this.users = users;
        this.hierarchy = hierarchy;
        this.settings = settings;
    }

    @Transactional(readOnly = true)
    public List<RoleView> listRoles() {
        return roles.findAll().stream().map(RoleView::from).toList();
    }

    public RoleView grantPermission(String roleName, String permission, String actor) {
        if (!PERMISSION.matcher(permission).matches()) {
            throw new IllegalArgumentException("Permission must look like resource:action");
        }
        Role role = role(roleName);
        role.getPermissions().add(permission);
        audit.info("GRANT {} -> {} by {}", permission, roleName, actor);
        return RoleView.from(role);
    }

    public RoleView revokePermission(String roleName, String permission, String actor) {
        Role role = role(roleName);
        role.getPermissions().remove(permission);
        audit.info("REVOKE {} from {} by {}", permission, roleName, actor);
        return RoleView.from(role);
    }

    /** Makes {@code parentName} inherit {@code roleName}; rejects cycles. */
    public RoleView setParent(String roleName, String parentName, String actor) {
        Role role = role(roleName);
        Role parent = role(parentName);
        for (Role r = parent; r != null; r = r.getParent()) {
            if (r.getName().equals(roleName)) {
                throw new IllegalArgumentException("Hierarchy cycle: " + parentName + " already inherits from " + roleName);
            }
        }
        role.setParent(parent);
        roles.flush();
        hierarchy.refresh();
        audit.info("HIERARCHY {} > {} by {}", parentName, roleName, actor);
        return RoleView.from(role);
    }

    public Set<String> assignRoles(String username, Set<String> roleNames, String actor) {
        AppUser user = user(username);
        user.setRoles(roleNames.stream().map(this::role).collect(Collectors.toSet()));
        audit.info("ASSIGN roles {} -> {} by {}", roleNames, username, actor);
        return roleNames;
    }

    public void updateAttributes(String username, UserAttributesRequest req, String actor) {
        AppUser user = user(username);
        if (req.circle() != null) user.setCircle(req.circle());
        if (req.employmentType() != null) user.setEmploymentType(req.employmentType());
        audit.info("ATTRIBUTES {} circle={} type={} by {}", username, user.getCircle(), user.getEmploymentType(), actor);
    }

    public void updateSetting(String key, String value, String actor) {
        settings.put(key, value);
        audit.info("SETTING {}={} by {}", key, value, actor);
    }

    private Role role(String name) {
        return roles.findById(name).orElseThrow(() -> new NotFoundException("Unknown role " + name));
    }

    private AppUser user(String username) {
        return users.findByUsername(username).orElseThrow(() -> new NotFoundException("Unknown user " + username));
    }
}
