# Open Policy Agent version of the Java policies (telecom.abac.engine=opa).
# Run:  opa run --server --addr :8181 policies/telecom.rego
# Test: opa test policies/
package telecom.authz

import rego.v1

default decision := {"allow": false, "reason": "no rule matched"}

decision := {"allow": true, "reason": "permitted"} if count(deny_reasons) == 0

decision := {"allow": false, "reason": concat("; ", deny_reasons)} if count(deny_reasons) > 0

is_staff if "AGENT" in input.subject.roles
is_admin if "ADMIN" in input.subject.roles

deny_reasons contains "customer does not own this line" if {
    not is_staff
    input.resource.type == "Subscriber"
    input.resource.ownerUsername != input.subject.username
}

deny_reasons contains "outside circle" if {
    is_staff
    not is_admin
    input.resource.circle
    input.resource.circle != input.subject.circle
}

deny_reasons contains "VIP line requires SUPERVISOR" if {
    input.resource.vip
    is_staff
    not "SUPERVISOR" in input.subject.roles
}

deny_reasons contains "contractor restriction" if {
    input.subject.employmentType == "CONTRACTOR"
    input.action in {"READ_PII", "EXPORT", "REFUND"}
}

deny_reasons contains "line is suspended" if {
    input.action in {"UPDATE", "REFUND"}
    input.resource.status == "SUSPENDED"
}

deny_reasons contains "exports only from INTERNAL channel" if {
    input.action == "EXPORT"
    upper(input.environment.channel) != "INTERNAL"
}

refund_limits := {"ADMIN": 10000, "SUPERVISOR": 1000}

deny_reasons contains "refund over limit" if {
    input.action == "REFUND"
    limit := max([refund_limits[r] | some r in input.subject.roles; refund_limits[r]])
    to_number(input.context.amount) > limit
}
