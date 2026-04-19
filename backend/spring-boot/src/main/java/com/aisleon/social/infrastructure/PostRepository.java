package com.aisleon.social.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<PostJpaEntity, UUID> {

    @Query(
            "SELECT p FROM PostJpaEntity p"
                    + " WHERE p.createdAt < :cursor"
                    + " AND p.reported = false"
                    + " ORDER BY p.createdAt DESC, p.id DESC")
    List<PostJpaEntity> findFeedBefore(LocalDateTime cursor, Pageable pageable);

    @Query(
            "SELECT p FROM PostJpaEntity p"
                    + " WHERE p.reported = false"
                    + " ORDER BY p.createdAt DESC, p.id DESC")
    List<PostJpaEntity> findFeedFirstPage(Pageable pageable);
}
