package com.telecom.access.security.abac;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

@ConfigurationProperties(prefix = "telecom.abac")
public record AbacProperties(String engine, ZoneId zoneId, String opaUrl) {
}
