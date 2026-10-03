# Telecom Access Control - Spring Security RBAC + ABAC

```bash
mvn spring-boot:run          # http://localhost:8080, HTTP Basic, password Telecom@123
mvn test                     # 23 tests: RBAC, ABAC, runtime changes, policy unit tests
```

| User | Role (inherits) | ABAC attributes |
|---|---|---|
| alice | CUSTOMER | owns 9876543210 |
| ravi | AGENT | circle TN, EMPLOYEE |
| sita | SUPERVISOR (> AGENT) | circle TN, EMPLOYEE |
| kiran | SUPERVISOR (> AGENT) | circle KA, CONTRACTOR |
| admin | ADMIN (> SUPERVISOR > AGENT) | circle ALL |

| Line | Circle | Attributes |
|---|---|---|
| 9876543210 | TN | retail, owner alice |
| 9876543211 | TN | VIP corporate |
| 9876543212 | KA | retail |
| 9876543213 | TN | SUSPENDED |

Policies (deny-overrides): customer-ownership, circle-boundary, vip-protection, contractor-restriction,
refund-limit, business-hours, line-status, export-channel. OPA equivalent: `policies/telecom.rego`
(`telecom.abac.engine=opa`). Walkthrough: `http/access-control.http`.
