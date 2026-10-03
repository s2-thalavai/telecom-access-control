package com.telecom.access.security.abac.policy;

import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Bulk exports only from the internal network/tools (environment attribute: channel). */
@Component
@Order(80)
public class ExportChannelPolicy implements AccessPolicy {

    public String name() { return "export-channel"; }

    public Decision evaluate(AccessRequest r) {
        if (!"EXPORT".equals(r.action())) return Decision.notApplicable();
        return "INTERNAL".equalsIgnoreCase(r.environment().channel())
                ? Decision.permit(name())
                : Decision.deny(name(), "exports allowed only from INTERNAL channel");
    }
}
