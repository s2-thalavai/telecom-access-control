package com.telecom.access.security.abac;

import com.telecom.access.entity.Subscriber;

/** WHAT is being accessed. type = "Subscriber" or "Report". */
public record ResourceAttributes(String type, String id, String circle, String status,
                                 boolean vip, String accountType, String ownerUsername) {

    public static ResourceAttributes of(Subscriber s) {
        return new ResourceAttributes("Subscriber", s.getMsisdn(), s.getCircle(), s.getStatus(),
                s.isVip(), s.getAccountType(), s.getOwnerUsername());
    }

    public static ResourceAttributes report(String name) {
        return new ResourceAttributes("Report", name, null, null, false, null, null);
    }
}
