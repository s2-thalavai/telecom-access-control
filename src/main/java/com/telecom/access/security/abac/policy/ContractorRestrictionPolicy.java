package com.telecom.access.security.abac.policy;

import com.telecom.access.entity.EmploymentType;
import com.telecom.access.security.abac.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Contractors never see full PII, export data or issue refunds - whatever their role. */
@Component
@Order(40)
public class ContractorRestrictionPolicy implements AccessPolicy {

    private static final Set<String> RESTRICTED = Set.of("READ_PII", "EXPORT", "REFUND");

    public String name() { return "contractor-restriction"; }

    public Decision evaluate(AccessRequest r) {
        if (r.subject().employmentType() != EmploymentType.CONTRACTOR || !RESTRICTED.contains(r.action())) {
            return Decision.notApplicable();
        }
        return Decision.deny(name(), "contractors may not perform " + r.action());
    }
}
