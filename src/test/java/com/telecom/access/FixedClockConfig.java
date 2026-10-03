package com.telecom.access;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** 10:00 IST (04:30 UTC) on a Wednesday: inside default business hours 08:00-20:00. */
@TestConfiguration
public class FixedClockConfig {
    @Bean
    @Primary
    Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-10-07T04:30:00Z"), ZoneOffset.UTC);
    }
}
