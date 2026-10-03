package com.telecom.access.security.abac;

import com.telecom.access.repository.SubscriberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Single entry point used by every enforcement point: builds the request and asks the PDP. */
@Service
public class AccessDecisionService {

    private final AttributeService attributes;
    private final PolicyDecisionPoint pdp;
    private final SubscriberRepository subscribers;

    public AccessDecisionService(AttributeService attributes, PolicyDecisionPoint pdp, SubscriberRepository subscribers) {
        this.attributes = attributes;
        this.pdp = pdp;
        this.subscribers = subscribers;
    }

    /**
     * Unknown subscribers are DENIED (403) rather than 404, so callers cannot probe which
     * numbers exist. Callers with a legitimate need still get a 404 from admin tools.
     */
    @Transactional(readOnly = true)
    public boolean canAccessSubscriber(Authentication auth, String msisdn, String action, Map<String, Object> context) {
        return subscribers.findByMsisdn(msisdn)
                .map(s -> decide(auth, action, ResourceAttributes.of(s), context))
                .orElse(false);
    }

    public boolean decide(Authentication auth, String action, ResourceAttributes resource, Map<String, Object> context) {
        return pdp.decide(attributes.build(auth, action, resource, context)).isPermit();
    }
}
