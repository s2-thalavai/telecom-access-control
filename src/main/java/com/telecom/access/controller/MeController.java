package com.telecom.access.controller;

import com.telecom.access.security.abac.AttributeService;
import com.telecom.access.security.abac.SubjectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shows the effective RBAC authorities and ABAC subject attributes of the caller. */
@RestController
public class MeController {

    private final AttributeService attributes;

    public MeController(AttributeService attributes) {
        this.attributes = attributes;
    }

    @GetMapping("/api/v1/me")
    public Map<String, Object> me(Authentication auth) {
        SubjectAttributes s = attributes.subject(auth);
        Map<String, Object> me = new LinkedHashMap<>();
        me.put("username", s.username());
        me.put("roles", s.roles());
        me.put("authorities", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList());
        me.put("circle", s.circle());
        me.put("employmentType", s.employmentType());
        me.put("environment", attributes.environment());
        return me;
    }
}
