package com.telecom.access.security.abac;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The full ABAC question: can SUBJECT perform ACTION on RESOURCE in ENVIRONMENT?
 * context carries action-specific attributes (e.g. refund amount).
 */
public record AccessRequest(SubjectAttributes subject, String action, ResourceAttributes resource,
                            EnvironmentAttributes environment, Map<String, Object> context) {

    public BigDecimal amount() {
        Object v = context.get("amount");
        return v == null ? null : new BigDecimal(v.toString());
    }
}
