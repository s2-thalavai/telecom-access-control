package com.telecom.access.security.abac;

/**
 * A single ABAC rule. Implementations are Spring beans, so adding a rule = adding a class.
 * Return NOT_APPLICABLE when the rule does not concern this request.
 */
public interface AccessPolicy {
    String name();
    Decision evaluate(AccessRequest request);
}
