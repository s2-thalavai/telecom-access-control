package com.telecom.access.controller;

import com.telecom.access.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Same URL, two media types with DIFFERENT access rules (see SecurityConfig):
 *   Accept: application/json -> summary, ROLE_SUPERVISOR
 *   Accept: text/csv         -> bulk export, report:export + ABAC (no contractors, INTERNAL channel)
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping(value = "/subscribers", produces = "application/json")
    public Map<String, Long> summary(Authentication auth) {
        return service.countByStatus(auth);
    }

    @GetMapping(value = "/subscribers", produces = "text/csv")
    public ResponseEntity<String> export(Authentication auth) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subscribers.csv")
                .body(service.csv(auth));
    }
}
