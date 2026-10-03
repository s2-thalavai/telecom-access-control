package com.telecom.access.security.abac;

/** PDP: decides; the PEPs (permission evaluator, @abac bean, URL AuthorizationManager) enforce. */
public interface PolicyDecisionPoint {
    Decision decide(AccessRequest request);
}
