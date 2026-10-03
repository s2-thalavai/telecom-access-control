package com.telecom.access.security.abac.policy;

import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/** Line suspension by non-admins only during business hours (environment attribute: time). */
@Component
@Order(60)
public class BusinessHoursPolicy implements AccessPolicy {

    private final PolicySettingsService settings;

    public BusinessHoursPolicy(PolicySettingsService settings) {
        this.settings = settings;
    }

    public String name() { return "business-hours"; }

    public Decision evaluate(AccessRequest r) {
        if (!"SUSPEND".equals(r.action()) || r.subject().isAdmin()) return Decision.notApplicable();
        LocalTime start = settings.time("business-hours.start", LocalTime.of(8, 0));
        LocalTime end = settings.time("business-hours.end", LocalTime.of(20, 0));
        LocalTime now = r.environment().localTime();
        return !now.isBefore(start) && now.isBefore(end)
                ? Decision.permit(name())
                : Decision.deny(name(), "outside business hours " + start + "-" + end);
    }
}
