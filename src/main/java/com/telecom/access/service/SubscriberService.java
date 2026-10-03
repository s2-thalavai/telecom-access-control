package com.telecom.access.service;

import com.telecom.access.entity.Subscriber;
import com.telecom.access.exception.NotFoundException;
import com.telecom.access.repository.SubscriberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SubscriberService {

    private static final Logger log = LoggerFactory.getLogger(SubscriberService.class);
    private final SubscriberRepository repository;

    public SubscriberService(SubscriberRepository repository) {
        this.repository = repository;
    }

    /**
     * Row-level ABAC: every element is checked with hasPermission(filterObject, 'READ');
     * elements the caller may not read are removed (circle, VIP, ownership policies).
     * For large tables push the filter into the query instead (see answer).
     */
    @Transactional(readOnly = true)
    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<Subscriber> listVisible() {
        return new ArrayList<>(repository.findAll());          // @PostFilter needs a mutable collection
    }

    @Transactional(readOnly = true)
    public Subscriber get(String msisdn) {
        return load(msisdn);
    }

    public Subscriber changePlan(String msisdn, String planCode, String actor) {
        Subscriber s = load(msisdn);
        log.info("AUDIT plan {} -> {} line={} by={}", s.getPlanCode(), planCode, msisdn, actor);
        s.setPlanCode(planCode);
        return s;
    }

    public Subscriber recharge(String msisdn, BigDecimal amount, String actor) {
        Subscriber s = load(msisdn);
        s.setBalance(s.getBalance().add(amount));
        log.info("AUDIT recharge {} line={} by={}", amount, msisdn, actor);
        return s;
    }

    public Subscriber refund(String msisdn, BigDecimal amount, String actor) {
        Subscriber s = load(msisdn);
        s.setBalance(s.getBalance().add(amount));
        log.info("AUDIT refund {} line={} by={}", amount, msisdn, actor);
        return s;
    }

    public Subscriber suspend(String msisdn, String actor) {
        Subscriber s = load(msisdn);
        s.setStatus("SUSPENDED");
        log.info("AUDIT suspend line={} by={}", msisdn, actor);
        return s;
    }

    private Subscriber load(String msisdn) {
        return repository.findByMsisdn(msisdn).orElseThrow(() -> new NotFoundException("Subscriber not found"));
    }
}
