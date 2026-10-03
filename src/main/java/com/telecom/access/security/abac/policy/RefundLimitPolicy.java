package com.telecom.access.security.abac.policy;

import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Refund amount limit depends on the caller's highest role. Limits live in policy_settings
 * (refund.limit.SUPERVISOR = 1000 ...) and can be changed at runtime.
 */
@Component
@Order(50)
public class RefundLimitPolicy implements AccessPolicy {

    private static final List<String> ROLE_PRECEDENCE = List.of("ADMIN", "SUPERVISOR", "AGENT");
    private final PolicySettingsService settings;

    public RefundLimitPolicy(PolicySettingsService settings) {
        this.settings = settings;
    }

    public String name() { return "refund-limit"; }

    public Decision evaluate(AccessRequest r) {
        if (!"REFUND".equals(r.action())) return Decision.notApplicable();
        BigDecimal amount = r.amount();
        if (amount == null) return Decision.deny(name(), "refund amount missing");

        String role = ROLE_PRECEDENCE.stream().filter(r.subject()::hasRole).findFirst().orElse(null);
        BigDecimal limit = role == null ? BigDecimal.ZERO
                : settings.decimal("refund.limit." + role, BigDecimal.ZERO);   // unknown -> 0 (deny by default)
        return amount.compareTo(limit) <= 0
                ? Decision.permit(name())
                : Decision.deny(name(), "amount " + amount + " exceeds " + role + " limit " + limit);
    }
}
