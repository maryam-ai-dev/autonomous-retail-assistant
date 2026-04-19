package com.aisleon.social.infrastructure;

import com.aisleon.social.domain.CommentTarget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentJpaEntity, UUID> {

    List<CommentJpaEntity> findByTargetTypeAndTargetIdAndReportedFalseOrderByCreatedAtDesc(
            CommentTarget targetType, UUID targetId);
}
