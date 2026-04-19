package com.aisleon.social.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aisleon.social.SelfFollowException;
import com.aisleon.social.infrastructure.FollowJpaEntity;
import com.aisleon.social.infrastructure.FollowRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FollowServiceTest {

    private final FollowRepository repo = mock(FollowRepository.class);
    private final FollowService service = new FollowService(repo);

    @Test
    void selfFollowThrows() {
        UUID me = UUID.randomUUID();
        assertThatThrownBy(() -> service.follow(me, me))
                .isInstanceOf(SelfFollowException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void firstFollowCreatesEdge() {
        UUID me = UUID.randomUUID(), other = UUID.randomUUID();
        when(repo.existsByFollowerUserIdAndFolloweeUserId(me, other)).thenReturn(false);

        service.follow(me, other);
        verify(repo).save(any(FollowJpaEntity.class));
    }

    @Test
    void secondFollowIsIdempotent() {
        UUID me = UUID.randomUUID(), other = UUID.randomUUID();
        when(repo.existsByFollowerUserIdAndFolloweeUserId(me, other)).thenReturn(true);

        service.follow(me, other);
        verify(repo, never()).save(any(FollowJpaEntity.class));
    }

    @Test
    void unfollowIsIdempotent() {
        UUID me = UUID.randomUUID(), other = UUID.randomUUID();
        service.unfollow(me, other);
        service.unfollow(me, other);
        verify(repo, org.mockito.Mockito.times(2))
                .deleteByFollowerUserIdAndFolloweeUserId(me, other);
    }

    @Test
    void unfollowSelfAlsoThrows() {
        UUID me = UUID.randomUUID();
        assertThatThrownBy(() -> service.unfollow(me, me))
                .isInstanceOf(SelfFollowException.class);
        verify(repo, never()).deleteByFollowerUserIdAndFolloweeUserId(eq(me), eq(me));
    }

    @Test
    void statusReportsFollowerAndFollowingCounts() {
        UUID me = UUID.randomUUID(), other = UUID.randomUUID();
        when(repo.existsByFollowerUserIdAndFolloweeUserId(me, other)).thenReturn(true);
        when(repo.countByFolloweeUserId(other)).thenReturn(7L);
        when(repo.countByFollowerUserId(other)).thenReturn(3L);

        var status = service.status(me, other);
        assertThat(status.following()).isTrue();
        assertThat(status.followerCount()).isEqualTo(7);
        assertThat(status.followingCount()).isEqualTo(3);
    }
}
