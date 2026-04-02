package com.aisleon.audit.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditEventJpaEntity, UUID> {

    List<AuditEventJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<AuditEventJpaEntity> findByUserIdAndEventTypeOrderByCreatedAtDesc(UUID userId, String eventType);
}
