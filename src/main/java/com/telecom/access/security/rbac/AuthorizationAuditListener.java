package com.telecom.access.security.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

/** Audit trail of every denied authorization (URL and method level). */
@Component
public class AuthorizationAuditListener {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @EventListener
    public void onDenied(AuthorizationDeniedEvent<?> event) {
        var auth = event.getAuthentication().get();
        audit.info("DENIED user={} target={} decision={}",
                auth != null ? auth.getName() : "anonymous",
                describe(event.getSource()),
                event.getAuthorizationDecision());
    }

    private static String describe(Object source) {
        if (source instanceof org.aopalliance.intercept.MethodInvocation mi) {
            return mi.getMethod().getDeclaringClass().getSimpleName() + "." + mi.getMethod().getName();
        }
        if (source instanceof jakarta.servlet.http.HttpServletRequest req) {
            return req.getMethod() + " " + req.getRequestURI();
        }
        return source.getClass().getSimpleName();
    }
}
