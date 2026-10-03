package com.telecom.access.dto;

import com.telecom.access.entity.Subscriber;
import java.math.BigDecimal;

/** Full PII representation (application/vnd.telecom.subscriber-full+json). */
public record SubscriberFullView(String msisdn, String fullName, String email, String address, String circle,
                                 String status, boolean vip, String accountType, String planCode,
                                 BigDecimal balance, String ownerUsername) {
    public static final String MEDIA_TYPE = "application/vnd.telecom.subscriber-full+json";

    public static SubscriberFullView from(Subscriber s) {
        return new SubscriberFullView(s.getMsisdn(), s.getFullName(), s.getEmail(), s.getAddress(), s.getCircle(),
                s.getStatus(), s.isVip(), s.getAccountType(), s.getPlanCode(), s.getBalance(), s.getOwnerUsername());
    }
}
