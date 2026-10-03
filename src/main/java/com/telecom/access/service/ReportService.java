package com.telecom.access.service;

import com.telecom.access.entity.Subscriber;
import com.telecom.access.repository.SubscriberRepository;
import com.telecom.access.security.abac.AttributeService;
import com.telecom.access.security.abac.SubjectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Reports apply the circle attribute in the QUERY (not by post-filtering), which scales to
 * millions of rows: admins see all circles, everyone else only their own.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final SubscriberRepository repository;
    private final AttributeService attributes;

    public ReportService(SubscriberRepository repository, AttributeService attributes) {
        this.repository = repository;
        this.attributes = attributes;
    }

    public Map<String, Long> countByStatus(Authentication auth) {
        return visible(auth).stream()
                .collect(Collectors.groupingBy(Subscriber::getStatus, TreeMap::new, Collectors.counting()));
    }

    public String csv(Authentication auth) {
        StringBuilder sb = new StringBuilder("msisdn,circle,status,plan,balance\n");
        visible(auth).forEach(s -> sb.append(String.join(",", s.getMsisdn(), s.getCircle(), s.getStatus(),
                s.getPlanCode(), s.getBalance().toPlainString())).append('\n'));
        return sb.toString();
    }

    private List<Subscriber> visible(Authentication auth) {
        SubjectAttributes subject = attributes.subject(auth);
        return subject.isAdmin() ? repository.findAll() : repository.findByCircle(subject.circle());
    }
}
