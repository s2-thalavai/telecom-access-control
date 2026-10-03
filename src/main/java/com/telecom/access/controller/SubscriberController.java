package com.telecom.access.controller;

import com.telecom.access.dto.*;
import com.telecom.access.security.rbac.SubscriberAccess;
import com.telecom.access.service.SubscriberService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscribers")
public class SubscriberController {

    private final SubscriberService service;

    public SubscriberController(SubscriberService service) {
        this.service = service;
    }

    /** RBAC gate (subscriber:list), then ABAC row filtering in the service (@PostFilter). */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('subscriber:list')")
    public List<SubscriberView> list() {
        return service.listVisible().stream().map(SubscriberView::from).toList();
    }

    /** application/json -> masked view. RBAC permission + ABAC READ via the meta-annotation. */
    @GetMapping(value = "/{msisdn}", produces = MediaType.APPLICATION_JSON_VALUE)
    @SubscriberAccess(permission = "subscriber:read", action = "READ")
    public SubscriberView get(@PathVariable String msisdn) {
        return SubscriberView.from(service.get(msisdn));
    }

    /**
     * Content negotiation: the FULL PII representation is a different, more sensitive resource.
     * RBAC: SUPERVISOR or above. ABAC: READ_PII (contractors denied, circle/VIP rules apply).
     */
    @GetMapping(value = "/{msisdn}", produces = SubscriberFullView.MEDIA_TYPE)
    @PreAuthorize("hasRole('SUPERVISOR') and hasPermission(#msisdn, 'Subscriber', 'READ_PII')")
    public SubscriberFullView getFull(@PathVariable String msisdn) {
        return SubscriberFullView.from(service.get(msisdn));
    }

    @PatchMapping("/{msisdn}/plan")
    @SubscriberAccess(permission = "plan:change", action = "UPDATE")
    public SubscriberView changePlan(@PathVariable String msisdn, @Valid @RequestBody ChangePlanRequest request,
                                     Authentication auth) {
        return SubscriberView.from(service.changePlan(msisdn, request.planCode(), auth.getName()));
    }

    @PostMapping("/{msisdn}/recharge")
    @SubscriberAccess(permission = "recharge:create", action = "UPDATE")
    public SubscriberView recharge(@PathVariable String msisdn, @Valid @RequestBody AmountRequest request,
                                   Authentication auth) {
        return SubscriberView.from(service.recharge(msisdn, request.amount(), auth.getName()));
    }

    /** ABAC with a request attribute (amount) -> custom bean instead of hasPermission. */
    @PostMapping("/{msisdn}/refund")
    @PreAuthorize("hasAuthority('recharge:refund') and @abac.canRefund(authentication, #msisdn, #request.amount())")
    public SubscriberView refund(@PathVariable String msisdn, @Valid @RequestBody AmountRequest request,
                                 Authentication auth) {
        return SubscriberView.from(service.refund(msisdn, request.amount(), auth.getName()));
    }

    @PostMapping("/{msisdn}/suspend")
    @SubscriberAccess(permission = "subscriber:suspend", action = "SUSPEND")
    public SubscriberView suspend(@PathVariable String msisdn, Authentication auth) {
        return SubscriberView.from(service.suspend(msisdn, auth.getName()));
    }
}
