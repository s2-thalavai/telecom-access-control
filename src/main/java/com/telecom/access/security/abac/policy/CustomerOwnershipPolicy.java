package com.telecom.access.security.abac.policy;

import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Customers may only touch lines they own. */
@Component
@Order(10)
public class CustomerOwnershipPolicy implements AccessPolicy {

    public String name() { return "customer-ownership"; }

    public Decision evaluate(AccessRequest r) {
        if (r.subject().isStaff() || !"Subscriber".equals(r.resource().type())) return Decision.notApplicable();
        return r.subject().username().equals(r.resource().ownerUsername())
                ? Decision.permit(name())
                : Decision.deny(name(), "customer does not own this line");
    }
}
