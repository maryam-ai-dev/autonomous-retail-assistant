package com.aisleon.social.application;

import com.aisleon.social.SelfFollowException;
import com.aisleon.social.infrastructure.FollowJpaEntity;
import com.aisleon.social.infrastructure.FollowRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    public record FollowStatus(
            boolean following,
            long followerCount,
            long followingCount) {}

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    @Transactional
    public FollowStatus follow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new SelfFollowException();
        }
        if (!followRepository.existsByFollowerUserIdAndFolloweeUserId(followerId, followeeId)) {
            followRepository.save(FollowJpaEntity.builder()
                    .followerUserId(followerId)
                    .followeeUserId(followeeId)
                    .build());
        }
        return status(followerId, followeeId);
    }

    @Transactional
    public FollowStatus unfollow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new SelfFollowException();
        }
        followRepository.deleteByFollowerUserIdAndFolloweeUserId(followerId, followeeId);
        return status(followerId, followeeId);
    }

    @Transactional(readOnly = true)
    public FollowStatus status(UUID followerId, UUID followeeId) {
        boolean following = followRepository
                .existsByFollowerUserIdAndFolloweeUserId(followerId, followeeId);
        long followerCount = followRepository.countByFolloweeUserId(followeeId);
        long followingCount = followRepository.countByFollowerUserId(followeeId);
        return new FollowStatus(following, followerCount, followingCount);
    }
}
