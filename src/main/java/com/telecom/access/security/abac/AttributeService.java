package com.telecom.access.security.abac;

import com.telecom.access.security.rbac.TelecomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Policy Information Point: collects subject, environment (and with the caller, resource) attributes. */
@Service
public class AttributeService {

    private final Clock clock;
    private final AbacProperties props;

    public AttributeService(Clock clock, AbacProperties props) {
        this.clock = clock;
        this.props = props;
    }

    public AccessRequest build(Authentication auth, String action, ResourceAttributes resource, Map<String, Object> context) {
        return new AccessRequest(subject(auth), action, resource, environment(), context);
    }

    public SubjectAttributes subject(Authentication auth) {
        Set<String> permissions = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_")).collect(Collectors.toSet());
        if (auth.getPrincipal() instanceof TelecomUserPrincipal p) {
            return new SubjectAttributes(p.username(), p.roles(), permissions, p.circle(), p.employmentType());
        }
        // e.g. test principals: derive roles from authorities, no other attributes
        Set<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5)).collect(Collectors.toSet());
        return new SubjectAttributes(auth.getName(), roles, permissions, null, null);
    }

    public EnvironmentAttributes environment() {
        ZonedDateTime now = ZonedDateTime.now(clock.withZone(props.zoneId()));
        HttpServletRequest req = currentRequest();
        return new EnvironmentAttributes(now.toLocalTime(), now.getDayOfWeek(),
                req != null ? headerOr(req, "X-Channel", "APP") : "INTERNAL",
                req != null ? req.getRemoteAddr() : null,
                req != null ? req.getHeader(HttpHeaders.ACCEPT) : null);
    }

    private static HttpServletRequest currentRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes sra
                ? sra.getRequest() : null;
    }

    private static String headerOr(HttpServletRequest req, String name, String def) {
        String v = req.getHeader(name);
        return v == null || v.isBlank() ? def : v;
    }
}
