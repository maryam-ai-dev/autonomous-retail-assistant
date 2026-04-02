package com.aisleon.audit.service;

import com.aisleon.audit.controller.AuditEventResponse;
import com.aisleon.audit.repository.AuditEventJpaEntity;
import com.aisleon.audit.repository.AuditRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public List<AuditEventResponse> getRecentEvents(UUID userId, int limit) {
        return auditRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditEventResponse> getEventsByType(UUID userId, String eventType) {
        return auditRepository.findByUserIdAndEventTypeOrderByCreatedAtDesc(userId, eventType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditEventResponse toResponse(AuditEventJpaEntity entity) {
        return AuditEventResponse.builder()
                .id(entity.getId().toString())
                .eventType(entity.getEventType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .payload(entity.getPayload())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
