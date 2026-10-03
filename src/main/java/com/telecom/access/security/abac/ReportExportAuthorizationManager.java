package com.telecom.access.security.abac;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

/** URL-level ABAC enforcement point for bulk exports (used in the HTTP filter chain). */
@Component
public class ReportExportAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final AccessDecisionService decisions;

    public ReportExportAuthorizationManager(AccessDecisionService decisions) {
        this.decisions = decisions;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext ctx) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) return new AuthorizationDecision(false);
        String report = ctx.getRequest().getRequestURI();
        return new AuthorizationDecision(
                decisions.decide(auth, "EXPORT", ResourceAttributes.report(report), Map.of()));
    }
}
