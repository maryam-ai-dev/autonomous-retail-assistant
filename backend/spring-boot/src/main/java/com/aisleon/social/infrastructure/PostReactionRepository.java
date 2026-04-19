package com.aisleon.social.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReactionRepository
        extends JpaRepository<PostReactionJpaEntity, UUID> {

    Optional<PostReactionJpaEntity> findByPostIdAndUserId(UUID postId, UUID userId);

    List<PostReactionJpaEntity> findByPostId(UUID postId);

    long countByPostId(UUID postId);
}
