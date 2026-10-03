package com.telecom.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TelecomAccessControlApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelecomAccessControlApplication.class, args);
    }
}
