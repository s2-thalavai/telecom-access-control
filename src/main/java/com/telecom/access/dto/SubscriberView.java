package com.telecom.access.dto;

import com.telecom.access.entity.Subscriber;
import java.math.BigDecimal;

/** Default representation (application/json): PII masked. */
public record SubscriberView(String msisdn, String fullName, String email, String circle,
                             String status, boolean vip, String planCode, BigDecimal balance) {
    public static SubscriberView from(Subscriber s) {
        return new SubscriberView(s.getMsisdn(), s.getFullName(), mask(s.getEmail()), s.getCircle(),
                s.getStatus(), s.isVip(), s.getPlanCode(), s.getBalance());
    }

    static String mask(String email) {
        if (email == null || !email.contains("@")) return null;
        return email.charAt(0) + "***" + email.substring(email.indexOf('@'));
    }
}
