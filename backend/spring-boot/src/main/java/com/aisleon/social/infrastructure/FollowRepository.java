package com.aisleon.social.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<FollowJpaEntity, UUID> {

    Optional<FollowJpaEntity> findByFollowerUserIdAndFolloweeUserId(
            UUID follower, UUID followee);

    long countByFollowerUserId(UUID followerUserId);

    long countByFolloweeUserId(UUID followeeUserId);

    void deleteByFollowerUserIdAndFolloweeUserId(UUID follower, UUID followee);

    boolean existsByFollowerUserIdAndFolloweeUserId(UUID follower, UUID followee);
}
