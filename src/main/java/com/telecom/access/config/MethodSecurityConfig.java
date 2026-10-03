package com.telecom.access.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.method.PrePostTemplateDefaults;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method security: @PreAuthorize / @PostAuthorize / @PostFilter with
 *  - the role hierarchy (hasRole('AGENT') is true for SUPERVISOR and ADMIN)
 *  - the ABAC PermissionEvaluator behind hasPermission(...)
 *  - meta-annotation templates (@SubscriberAccess(permission=..., action=...))
 * Beans are static and dependencies @Lazy so infrastructure is created before application beans.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(@Lazy RoleHierarchy roleHierarchy,
                                                                           @Lazy PermissionEvaluator permissionEvaluator,
                                                                           ApplicationContext context) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);
        handler.setApplicationContext(context);       // enables @beanName references in SpEL
        return handler;
    }

    @Bean
    static PrePostTemplateDefaults prePostTemplateDefaults() {
        return new PrePostTemplateDefaults();
    }
}
