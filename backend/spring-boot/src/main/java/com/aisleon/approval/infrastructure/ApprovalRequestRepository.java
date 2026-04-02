package com.aisleon.approval.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequestJpaEntity, UUID> {

    List<ApprovalRequestJpaEntity> findByUserId(UUID userId);

    List<ApprovalRequestJpaEntity> findByUserIdAndStatus(UUID userId, String status);

    List<ApprovalRequestJpaEntity> findByCartId(UUID cartId);
}
