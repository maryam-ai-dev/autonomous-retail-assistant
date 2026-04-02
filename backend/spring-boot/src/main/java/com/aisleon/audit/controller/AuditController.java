package com.aisleon.audit.controller;

import com.aisleon.audit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditEventResponse>> getAuditEvents(
            Authentication authentication,
            @RequestParam(required = false) String type) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());

        List<AuditEventResponse> events;
        if (type != null && !type.isBlank()) {
            events = auditService.getEventsByType(userId, type);
        } else {
            events = auditService.getRecentEvents(userId, 50);
        }

        return ResponseEntity.ok(events);
    }
}
