package com.telecom.access.security.abac.policy;

import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Staff work only on subscribers of their own circle (region); head-office admins are exempt. */
@Component
@Order(20)
public class CircleBoundaryPolicy implements AccessPolicy {

    public String name() { return "circle-boundary"; }

    public Decision evaluate(AccessRequest r) {
        if (!r.subject().isStaff() || r.subject().isAdmin() || r.resource().circle() == null) {
            return Decision.notApplicable();
        }
        return r.resource().circle().equals(r.subject().circle())
                ? Decision.permit(name())
                : Decision.deny(name(), "subscriber is in circle " + r.resource().circle());
    }
}
