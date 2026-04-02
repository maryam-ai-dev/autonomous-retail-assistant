package com.aisleon.audit.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {

    private String id;
    private String eventType;
    private String entityType;
    private String entityId;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;
}
