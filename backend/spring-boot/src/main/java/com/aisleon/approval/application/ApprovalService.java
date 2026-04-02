package com.aisleon.approval.application;

import com.aisleon.approval.domain.ApprovalRequest;
import com.aisleon.approval.events.ApprovalRequestedEvent;
import com.aisleon.approval.events.PurchaseAuthorizedEvent;
import com.aisleon.approval.events.PurchaseRejectedEvent;
import com.aisleon.approval.infrastructure.ApprovalRequestJpaEntity;
import com.aisleon.approval.infrastructure.ApprovalRequestMapper;
import com.aisleon.approval.infrastructure.ApprovalRequestRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ApprovalService {

    private final ApprovalRequestRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public ApprovalService(ApprovalRequestRepository repository,
                           ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ApprovalRequest createApprovalRequest(UUID userId, UUID cartId,
                                                  String reason, BigDecimal amount) {
        ApprovalRequestJpaEntity entity = ApprovalRequestJpaEntity.builder()
                .userId(userId)
                .cartId(cartId)
                .triggerReason(reason)
                .totalAmount(amount)
                .build();

        ApprovalRequestJpaEntity saved = repository.save(entity);
        ApprovalRequest domain = ApprovalRequestMapper.toDomain(saved);

        eventPublisher.publishEvent(new ApprovalRequestedEvent(
                domain.getId(), userId, cartId, reason, amount
        ));

        return domain;
    }

    @Transactional
    public ApprovalRequest approve(UUID approvalId, UUID userId) {
        ApprovalRequestJpaEntity entity = repository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval request not found: " + approvalId));

        ApprovalRequest domain = ApprovalRequestMapper.toDomain(entity);
        domain.approve();

        repository.save(ApprovalRequestMapper.toEntity(domain));

        eventPublisher.publishEvent(new PurchaseAuthorizedEvent(
                domain.getId(), userId, domain.getCartId(), domain.getTotalAmount()
        ));

        return domain;
    }

    @Transactional
    public ApprovalRequest reject(UUID approvalId, UUID userId) {
        ApprovalRequestJpaEntity entity = repository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval request not found: " + approvalId));

        ApprovalRequest domain = ApprovalRequestMapper.toDomain(entity);
        domain.reject();

        repository.save(ApprovalRequestMapper.toEntity(domain));

        eventPublisher.publishEvent(new PurchaseRejectedEvent(
                domain.getId(), userId, domain.getCartId(), domain.getTotalAmount()
        ));

        return domain;
    }

    public List<ApprovalRequest> getPendingApprovals(UUID userId) {
        return repository.findByUserIdAndStatus(userId, "PENDING").stream()
                .map(ApprovalRequestMapper::toDomain)
                .toList();
    }

    public ApprovalRequest getApprovalById(UUID id) {
        ApprovalRequestJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval request not found: " + id));
        return ApprovalRequestMapper.toDomain(entity);
    }
}
