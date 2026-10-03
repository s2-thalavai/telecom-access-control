package com.telecom.access.security.abac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * External PDP: Open Policy Agent. Policies live in policies/telecom.rego and are versioned,
 * tested and deployed independently of the Java service. Enable with telecom.abac.engine=opa.
 * POST {"input": {...}} -> {"result": {"allow": true|false, "reason": "..."}}
 */
@Component
@ConditionalOnProperty(prefix = "telecom.abac", name = "engine", havingValue = "opa")
public class OpaPolicyDecisionPoint implements PolicyDecisionPoint {

    private static final Logger log = LoggerFactory.getLogger(OpaPolicyDecisionPoint.class);
    private final RestClient client;

    public OpaPolicyDecisionPoint(RestClient.Builder builder, AbacProperties props) {
        this.client = builder.baseUrl(props.opaUrl()).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Decision decide(AccessRequest request) {
        try {
            Map<String, Object> body = client.post()
                    .body(Map.of("input", request))
                    .retrieve()
                    .body(Map.class);
            Map<String, Object> result = body == null ? Map.of() : (Map<String, Object>) body.getOrDefault("result", Map.of());
            boolean allow = Boolean.TRUE.equals(result.get("allow"));
            return allow ? Decision.permit("opa")
                         : Decision.deny("opa", String.valueOf(result.getOrDefault("reason", "denied by OPA")));
        } catch (RuntimeException ex) {
            log.error("OPA unreachable - failing closed", ex);
            return Decision.deny("opa", "policy engine unavailable");
        }
    }
}
