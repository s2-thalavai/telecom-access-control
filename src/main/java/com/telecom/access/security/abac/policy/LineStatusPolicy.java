package com.telecom.access.security.abac.policy;

import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/** No commercial changes on a suspended line (resource attribute: status). */
@Component
@Order(70)
public class LineStatusPolicy implements AccessPolicy {

    private static final Set<String> BLOCKED_ACTIONS = Set.of("UPDATE", "REFUND");

    public String name() { return "line-status"; }

    public Decision evaluate(AccessRequest r) {
        if (!BLOCKED_ACTIONS.contains(r.action()) || !"SUSPENDED".equals(r.resource().status())) {
            return Decision.notApplicable();
        }
        return Decision.deny(name(), "line is suspended");
    }
}
