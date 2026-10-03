package com.telecom.access.config;

import com.telecom.access.security.abac.ReportExportAuthorizationManager;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Set;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * URL-level (coarse) rules. Fine-grained rules live on service/controller methods.
 * Authentication is HTTP Basic against the DB so that this project focuses on AUTHORIZATION;
 * the same rules work unchanged with JWT/OAuth2 (see previous projects).
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            ReportExportAuthorizationManager exportAbac) throws Exception {
        // Media-type aware rule: CSV downloads of reports are a bulk export -> stricter policy
        MediaTypeRequestMatcher csv = new MediaTypeRequestMatcher(MediaType.valueOf("text/csv"));
        csv.setIgnoredMediaTypes(Set.of(MediaType.ALL));          // "Accept: */*" is NOT a CSV request
        RequestMatcher csvReports = new AndRequestMatcher(antMatcher("/api/v1/reports/**"), csv);

        http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                    // RBAC: only ADMIN (and anything above it) may manage roles/policies
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    // RBAC + ABAC combined on a URL: permission AND export policies (contractor, channel)
                    .requestMatchers(csvReports).access(AuthorizationManagers.allOf(
                            AuthorityAuthorizationManager.<RequestAuthorizationContext>hasAuthority("report:export"),
                            exportAbac))
                    // JSON reports: SUPERVISOR or above (hierarchy expands ADMIN -> SUPERVISOR)
                    .requestMatchers("/api/v1/reports/**").hasRole("SUPERVISOR")
                    .requestMatchers("/api/v1/**").authenticated()
                    .anyRequest().denyAll())                   // default deny
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Publishes AuthorizationDeniedEvent for both URL and method checks (consumed by the audit listener). */
    @Bean
    AuthorizationEventPublisher authorizationEventPublisher(ApplicationEventPublisher publisher) {
        return new SpringAuthorizationEventPublisher(publisher);
    }
}
