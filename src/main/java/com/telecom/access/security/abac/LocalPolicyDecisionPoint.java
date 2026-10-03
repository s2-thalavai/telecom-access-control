package com.telecom.access.security.abac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * In-process PDP with the DENY-OVERRIDES combining algorithm:
 * any applicable DENY wins; otherwise PERMIT. ABAC here RESTRICTS what RBAC already allowed,
 * so "no applicable policy" means the RBAC grant stands.
 */
@Component
@ConditionalOnProperty(prefix = "telecom.abac", name = "engine", havingValue = "local", matchIfMissing = true)
public class LocalPolicyDecisionPoint implements PolicyDecisionPoint {

    private static final Logger log = LoggerFactory.getLogger(LocalPolicyDecisionPoint.class);
    private final List<AccessPolicy> policies;            // ordered by @Order

    public LocalPolicyDecisionPoint(List<AccessPolicy> policies) {
        this.policies = policies;
    }

    @Override
    public Decision decide(AccessRequest request) {
        for (AccessPolicy policy : policies) {
            Decision d;
            try {
                d = policy.evaluate(request);
            } catch (RuntimeException ex) {                 // a broken policy must fail CLOSED
                log.error("Policy {} failed", policy.name(), ex);
                d = Decision.deny(policy.name(), "policy error");
            }
            if (d.effect() == Decision.Effect.DENY) {
                log.info("ABAC DENY user={} action={} resource={}:{} policy={} reason={}",
                        request.subject().username(), request.action(), request.resource().type(),
                        request.resource().id(), d.policy(), d.reason());
                return d;
            }
        }
        log.debug("ABAC PERMIT user={} action={} resource={}", request.subject().username(),
                request.action(), request.resource().id());
        return Decision.permit("default");
    }
}
