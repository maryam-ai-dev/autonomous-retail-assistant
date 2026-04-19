package com.aisleon.social.application;

import com.aisleon.social.PostNotFoundException;
import com.aisleon.social.api.CreatePostRequest;
import com.aisleon.social.api.FeedPageDto;
import com.aisleon.social.api.PostDto;
import com.aisleon.social.domain.ReactionType;
import com.aisleon.social.infrastructure.PostJpaEntity;
import com.aisleon.social.infrastructure.PostReactionJpaEntity;
import com.aisleon.social.infrastructure.PostReactionRepository;
import com.aisleon.social.infrastructure.PostRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    public static final int FEED_PAGE_SIZE = 20;

    private final PostRepository postRepository;
    private final PostReactionRepository reactionRepository;

    public PostService(
            PostRepository postRepository, PostReactionRepository reactionRepository) {
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
    }

    @Transactional
    public PostDto create(UUID userId, CreatePostRequest request) {
        PostJpaEntity entity = PostJpaEntity.builder()
                .userId(userId)
                .postType(request.getPostType())
                .basketId(request.getBasketId())
                .externalProductId(request.getExternalProductId())
                .body(request.getBody() == null ? "" : request.getBody())
                .imageUrl(request.getImageUrl())
                .reported(false)
                .reportedCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        PostJpaEntity saved = postRepository.save(entity);
        return toDto(saved, userId);
    }

    @Transactional(readOnly = true)
    public PostDto get(UUID postId, UUID viewerId) {
        PostJpaEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return toDto(post, viewerId);
    }

    @Transactional(readOnly = true)
    public FeedPageDto feed(UUID viewerId, String cursor) {
        PageRequest pageable = PageRequest.of(0, FEED_PAGE_SIZE);
        List<PostJpaEntity> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.findFeedFirstPage(pageable);
        } else {
            LocalDateTime cursorTs;
            try {
                cursorTs = LocalDateTime.parse(cursor, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                cursorTs = LocalDateTime.now();
            }
            posts = postRepository.findFeedBefore(cursorTs, pageable);
        }

        // Defensive dedupe by id.
        LinkedHashSet<UUID> seen = new LinkedHashSet<>();
        List<PostJpaEntity> unique = new java.util.ArrayList<>();
        for (PostJpaEntity p : posts) {
            if (seen.add(p.getId())) unique.add(p);
        }

        List<PostDto> dtos = unique.stream().map(p -> toDto(p, viewerId)).toList();
        boolean hasMore = unique.size() == FEED_PAGE_SIZE;
        String nextCursor = hasMore && !unique.isEmpty()
                ? unique.get(unique.size() - 1).getCreatedAt().format(
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        return FeedPageDto.builder()
                .posts(dtos)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    @Transactional
    public PostDto react(UUID postId, UUID userId, ReactionType type) {
        PostJpaEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        Optional<PostReactionJpaEntity> existing =
                reactionRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isEmpty()) {
            reactionRepository.save(PostReactionJpaEntity.builder()
                    .postId(postId)
                    .userId(userId)
                    .reactionType(type)
                    .build());
        } else if (existing.get().getReactionType() == type) {
            reactionRepository.delete(existing.get());
        } else {
            PostReactionJpaEntity entity = existing.get();
            entity.setReactionType(type);
            reactionRepository.save(entity);
        }
        return toDto(post, userId);
    }

    private PostDto toDto(PostJpaEntity post, UUID viewerId) {
        List<PostReactionJpaEntity> reactions = reactionRepository.findByPostId(post.getId());
        Map<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
        for (ReactionType t : ReactionType.values()) counts.put(t, 0L);
        ReactionType viewerReaction = null;
        for (PostReactionJpaEntity r : reactions) {
            counts.merge(r.getReactionType(), 1L, Long::sum);
            if (viewerId != null && viewerId.equals(r.getUserId())) {
                viewerReaction = r.getReactionType();
            }
        }
        // Preserve enum order in JSON.
        Map<ReactionType, Long> ordered = new LinkedHashMap<>();
        for (ReactionType t : ReactionType.values()) ordered.put(t, counts.get(t));
        return PostDto.fromEntity(post, ordered, viewerReaction);
    }
}
