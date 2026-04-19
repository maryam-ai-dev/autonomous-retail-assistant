package com.aisleon.social.application;

import com.aisleon.social.CommentTooLongException;
import com.aisleon.social.domain.CommentTarget;
import com.aisleon.social.infrastructure.CommentJpaEntity;
import com.aisleon.social.infrastructure.CommentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    public static final int MAX_BODY_LENGTH = 500;

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public CommentJpaEntity create(
            UUID authorUserId, CommentTarget targetType, UUID targetId, String body) {
        String trimmed = body == null ? "" : body.strip();
        if (trimmed.length() > MAX_BODY_LENGTH) {
            throw new CommentTooLongException(trimmed.length());
        }
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("comment body cannot be empty");
        }
        CommentJpaEntity entity = CommentJpaEntity.builder()
                .targetType(targetType)
                .targetId(targetId)
                .authorUserId(authorUserId)
                .body(trimmed)
                .reported(false)
                .reportedCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        return commentRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<CommentJpaEntity> listFor(CommentTarget targetType, UUID targetId) {
        return commentRepository
                .findByTargetTypeAndTargetIdAndReportedFalseOrderByCreatedAtDesc(
                        targetType, targetId);
    }
}
