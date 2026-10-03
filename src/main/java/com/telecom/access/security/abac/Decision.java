package com.telecom.access.security.abac;

/** Result of a policy evaluation, with the policy name and reason for auditing. */
public record Decision(Effect effect, String policy, String reason) {

    public enum Effect { PERMIT, DENY, NOT_APPLICABLE }

    public static Decision permit(String policy) { return new Decision(Effect.PERMIT, policy, "permitted"); }
    public static Decision deny(String policy, String reason) { return new Decision(Effect.DENY, policy, reason); }
    public static Decision notApplicable() { return new Decision(Effect.NOT_APPLICABLE, null, null); }

    public boolean isPermit() { return effect == Effect.PERMIT; }
}
