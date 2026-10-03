package com.telecom.access.security.abac;

import com.telecom.access.entity.Subscriber;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * Bridges Spring Security's hasPermission(...) SpEL to the ABAC engine:
 *   hasPermission(#msisdn, 'Subscriber', 'READ')       - by id (before loading)
 *   hasPermission(filterObject, 'READ')                - by object (@PostFilter / @PostAuthorize)
 */
@Component
public class TelecomPermissionEvaluator implements PermissionEvaluator {

    private final AccessDecisionService decisions;

    public TelecomPermissionEvaluator(AccessDecisionService decisions) {
        this.decisions = decisions;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object target, Object permission) {
        if (target instanceof Subscriber s) {
            return decisions.decide(auth, permission.toString(), ResourceAttributes.of(s), Map.of());
        }
        return false;                                 // deny unknown types by default
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if ("Subscriber".equals(targetType) && targetId != null) {
            return decisions.canAccessSubscriber(auth, targetId.toString(), permission.toString(), Map.of());
        }
        return false;
    }
}
