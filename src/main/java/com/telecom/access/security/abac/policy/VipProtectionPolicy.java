package com.telecom.access.security.abac.policy;

import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** VIP lines (corporate CXOs, government) are handled by supervisors and above only. */
@Component
@Order(30)
public class VipProtectionPolicy implements AccessPolicy {

    public String name() { return "vip-protection"; }

    public Decision evaluate(AccessRequest r) {
        if (!r.resource().vip() || !r.subject().isStaff()) return Decision.notApplicable();
        return r.subject().hasRole("SUPERVISOR")
                ? Decision.permit(name())
                : Decision.deny(name(), "VIP line requires SUPERVISOR");
    }
}
