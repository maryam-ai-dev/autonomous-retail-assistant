package com.aisleon.social.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aisleon.social.PostNotFoundException;
import com.aisleon.social.api.CreatePostRequest;
import com.aisleon.social.api.FeedPageDto;
import com.aisleon.social.api.PostDto;
import com.aisleon.social.domain.PostType;
import com.aisleon.social.domain.ReactionType;
import com.aisleon.social.infrastructure.PostJpaEntity;
import com.aisleon.social.infrastructure.PostReactionJpaEntity;
import com.aisleon.social.infrastructure.PostReactionRepository;
import com.aisleon.social.infrastructure.PostRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class PostServiceTest {

    private final PostRepository postRepo = mock(PostRepository.class);
    private final PostReactionRepository reactionRepo = mock(PostReactionRepository.class);

    private final PostService service = new PostService(postRepo, reactionRepo);

    @Test
    void createReturnsPostWithZeroReactions() {
        UUID userId = UUID.randomUUID();
        when(postRepo.save(any())).thenAnswer(inv -> {
            PostJpaEntity p = inv.getArgument(0);
            if (p.getId() == null) p.setId(UUID.randomUUID());
            return p;
        });
        when(reactionRepo.findByPostId(any())).thenReturn(List.of());

        CreatePostRequest request = new CreatePostRequest(
                PostType.TEXT, null, null, "first post!", null);
        PostDto dto = service.create(userId, request);

        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getReactionCounts())
                .containsEntry(ReactionType.TRIED_THIS, 0L)
                .containsEntry(ReactionType.BETTER_ALT, 0L)
                .containsEntry(ReactionType.WOULDNT_RECOMMEND, 0L);
        assertThat(dto.getViewerReaction()).isNull();
    }

    @Test
    void reactionToggleAdds_RemovesSameType_ChangesToOther() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PostJpaEntity post = stubExistingPost(postId);

        // 1. Not yet reacted → creates
        when(reactionRepo.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());
        when(reactionRepo.findByPostId(postId)).thenReturn(List.of());
        service.react(postId, userId, ReactionType.TRIED_THIS);
        verify(reactionRepo).save(any(PostReactionJpaEntity.class));

        // 2. Same type again → deletes (toggle off)
        PostReactionJpaEntity existingTried = PostReactionJpaEntity.builder()
                .id(UUID.randomUUID())
                .postId(postId)
                .userId(userId)
                .reactionType(ReactionType.TRIED_THIS)
                .build();
        when(reactionRepo.findByPostIdAndUserId(postId, userId))
                .thenReturn(Optional.of(existingTried));
        service.react(postId, userId, ReactionType.TRIED_THIS);
        verify(reactionRepo).delete(existingTried);

        // 3. Different type → updates in place
        PostReactionJpaEntity existingBetter = PostReactionJpaEntity.builder()
                .id(UUID.randomUUID())
                .postId(postId)
                .userId(userId)
                .reactionType(ReactionType.TRIED_THIS)
                .build();
        when(reactionRepo.findByPostIdAndUserId(postId, userId))
                .thenReturn(Optional.of(existingBetter));
        service.react(postId, userId, ReactionType.BETTER_ALT);
        assertThat(existingBetter.getReactionType()).isEqualTo(ReactionType.BETTER_ALT);
    }

    @Test
    void feedFirstPageReturnsHasMoreWhenFullPage() {
        UUID viewerId = UUID.randomUUID();
        List<PostJpaEntity> posts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < PostService.FEED_PAGE_SIZE; i++) {
            posts.add(stubPost(UUID.randomUUID(), now.minusMinutes(i)));
        }
        when(postRepo.findFeedFirstPage(any(Pageable.class))).thenReturn(posts);
        when(reactionRepo.findByPostId(any())).thenReturn(List.of());

        FeedPageDto page = service.feed(viewerId, null);

        assertThat(page.isHasMore()).isTrue();
        assertThat(page.getNextCursor()).isNotNull();
        assertThat(page.getPosts()).hasSize(PostService.FEED_PAGE_SIZE);
    }

    @Test
    void feedLastPageHasNoMore() {
        UUID viewerId = UUID.randomUUID();
        List<PostJpaEntity> posts = List.of(
                stubPost(UUID.randomUUID(), LocalDateTime.now()),
                stubPost(UUID.randomUUID(), LocalDateTime.now().minusMinutes(1)));
        when(postRepo.findFeedFirstPage(any(Pageable.class))).thenReturn(posts);
        when(reactionRepo.findByPostId(any())).thenReturn(List.of());

        FeedPageDto page = service.feed(viewerId, null);

        assertThat(page.isHasMore()).isFalse();
        assertThat(page.getNextCursor()).isNull();
    }

    @Test
    void feedDuplicatesAreDedupedBeforeReturning() {
        UUID sharedId = UUID.randomUUID();
        List<PostJpaEntity> posts = new ArrayList<>();
        posts.add(stubPost(sharedId, LocalDateTime.now()));
        posts.add(stubPost(sharedId, LocalDateTime.now().minusSeconds(1)));
        posts.add(stubPost(UUID.randomUUID(), LocalDateTime.now().minusSeconds(2)));
        when(postRepo.findFeedFirstPage(any(Pageable.class))).thenReturn(posts);
        when(reactionRepo.findByPostId(any())).thenReturn(List.of());

        FeedPageDto page = service.feed(UUID.randomUUID(), null);

        assertThat(page.getPosts()).hasSize(2);
    }

    @Test
    void getThrowsWhenMissing() {
        UUID missing = UUID.randomUUID();
        when(postRepo.findById(missing)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(missing, UUID.randomUUID()))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void reactThrowsWhenPostMissing() {
        UUID missing = UUID.randomUUID();
        when(postRepo.findById(missing)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.react(missing, UUID.randomUUID(), ReactionType.TRIED_THIS))
                .isInstanceOf(PostNotFoundException.class);
        verify(reactionRepo, never()).save(any());
    }

    private PostJpaEntity stubExistingPost(UUID id) {
        PostJpaEntity post = stubPost(id, LocalDateTime.now());
        when(postRepo.findById(id)).thenReturn(Optional.of(post));
        return post;
    }

    private PostJpaEntity stubPost(UUID id, LocalDateTime when) {
        return PostJpaEntity.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .postType(PostType.TEXT)
                .body("hello")
                .reported(false)
                .reportedCount(0)
                .createdAt(when)
                .build();
    }
}
