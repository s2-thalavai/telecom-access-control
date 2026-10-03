package com.telecom.access;

import com.telecom.access.entity.EmploymentType;
import com.telecom.access.security.abac.*;
import com.telecom.access.security.abac.policy.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Policies are plain classes: test them as a decision table, no Spring context needed. */
class PolicyUnitTest {

    private static final SubjectAttributes AGENT_TN =
            new SubjectAttributes("ravi", Set.of("AGENT"), Set.of(), "TN", EmploymentType.EMPLOYEE);
    private static final SubjectAttributes SUP_TN =
            new SubjectAttributes("sita", Set.of("SUPERVISOR", "AGENT"), Set.of(), "TN", EmploymentType.EMPLOYEE);
    private static final ResourceAttributes TN_LINE =
            new ResourceAttributes("Subscriber", "1", "TN", "ACTIVE", false, "RETAIL", null);
    private static final ResourceAttributes KA_VIP =
            new ResourceAttributes("Subscriber", "2", "KA", "ACTIVE", true, "CORPORATE", null);
    private static final EnvironmentAttributes TEN_AM =
            new EnvironmentAttributes(LocalTime.of(10, 0), DayOfWeek.WEDNESDAY, "APP", null, null);

    private static AccessRequest req(SubjectAttributes s, String action, ResourceAttributes r, Map<String, Object> ctx) {
        return new AccessRequest(s, action, r, TEN_AM, ctx);
    }

    @Test
    void refundLimitDecisionTable() {
        PolicySettingsService settings = mock(PolicySettingsService.class);
        when(settings.decimal("refund.limit.SUPERVISOR", BigDecimal.ZERO)).thenReturn(new BigDecimal("1000"));
        when(settings.decimal("refund.limit.AGENT", BigDecimal.ZERO)).thenReturn(BigDecimal.ZERO);
        RefundLimitPolicy policy = new RefundLimitPolicy(settings);

        assertThat(policy.evaluate(req(SUP_TN, "REFUND", TN_LINE, Map.of("amount", 1000))).isPermit()).isTrue();
        assertThat(policy.evaluate(req(SUP_TN, "REFUND", TN_LINE, Map.of("amount", 1001))).effect())
                .isEqualTo(Decision.Effect.DENY);
        assertThat(policy.evaluate(req(AGENT_TN, "REFUND", TN_LINE, Map.of("amount", 1))).effect())
                .isEqualTo(Decision.Effect.DENY);
        assertThat(policy.evaluate(req(SUP_TN, "READ", TN_LINE, Map.of())).effect())
                .isEqualTo(Decision.Effect.NOT_APPLICABLE);
    }

    @Test
    void denyOverridesCombining() {
        PolicySettingsService settings = mock(PolicySettingsService.class);
        when(settings.get(anyString())).thenReturn(Optional.empty());
        LocalPolicyDecisionPoint pdp = new LocalPolicyDecisionPoint(List.of(
                new CircleBoundaryPolicy(), new VipProtectionPolicy(), new BusinessHoursPolicy(settings)));

        assertThat(pdp.decide(req(AGENT_TN, "READ", TN_LINE, Map.of())).isPermit()).isTrue();
        Decision d = pdp.decide(req(SUP_TN, "READ", KA_VIP, Map.of()));     // VIP ok for SUP, but wrong circle
        assertThat(d.isPermit()).isFalse();
        assertThat(d.policy()).isEqualTo("circle-boundary");
    }

    @Test
    void brokenPolicyFailsClosed() {
        AccessPolicy broken = new AccessPolicy() {
            public String name() { return "broken"; }
            public Decision evaluate(AccessRequest r) { throw new IllegalStateException("boom"); }
        };
        LocalPolicyDecisionPoint pdp = new LocalPolicyDecisionPoint(List.of(broken));
        assertThat(pdp.decide(req(AGENT_TN, "READ", TN_LINE, Map.of())).isPermit()).isFalse();
    }
}
