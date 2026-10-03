package com.telecom.access.security.abac;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * SpEL-callable bean for decisions that need extra attributes from the request,
 * e.g. @PreAuthorize("@abac.canRefund(authentication, #msisdn, #request.amount())").
 */
@Component("abac")
public class AbacAuthorization {

    private final AccessDecisionService decisions;

    public AbacAuthorization(AccessDecisionService decisions) {
        this.decisions = decisions;
    }

    public boolean canRefund(Authentication auth, String msisdn, BigDecimal amount) {
        return decisions.canAccessSubscriber(auth, msisdn, "REFUND", Map.of("amount", amount));
    }

    public boolean can(Authentication auth, String action, String msisdn) {
        return decisions.canAccessSubscriber(auth, msisdn, action, Map.of());
    }
}
