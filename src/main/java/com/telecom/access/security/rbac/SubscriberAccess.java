package com.telecom.access.security.rbac;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Meta-annotation combining RBAC + ABAC in one readable line:
 *   @SubscriberAccess(permission = "plan:change", action = "UPDATE")
 * expands to
 *   hasAuthority('plan:change') and hasPermission(#msisdn, 'Subscriber', 'UPDATE')
 * (template placeholders enabled by the PrePostTemplateDefaults bean).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAuthority('{permission}') and hasPermission(#msisdn, 'Subscriber', '{action}')")
public @interface SubscriberAccess {
    String permission();
    String action();
}
