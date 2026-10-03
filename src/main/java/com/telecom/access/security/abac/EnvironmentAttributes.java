package com.telecom.access.security.abac;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** WHEN / WHERE / HOW: local time in the operator's zone, channel header, client IP, media type. */
public record EnvironmentAttributes(LocalTime localTime, DayOfWeek dayOfWeek, String channel,
                                    String clientIp, String acceptedMediaType) {
}
