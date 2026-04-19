package com.aisleon.social.api;

import com.aisleon.social.domain.PostType;
import com.aisleon.social.domain.ReactionType;
import com.aisleon.social.infrastructure.PostJpaEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A post in the social feed")
public class PostDto {

    private UUID id;
    private UUID userId;
    private PostType postType;
    private UUID basketId;
    private String externalProductId;
    private String body;
    private String imageUrl;
    private boolean reported;
    private Map<ReactionType, Long> reactionCounts;
    private ReactionType viewerReaction;
    private LocalDateTime createdAt;

    public static PostDto fromEntity(
            PostJpaEntity post,
            Map<ReactionType, Long> counts,
            ReactionType viewerReaction) {
        return PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .postType(post.getPostType())
                .basketId(post.getBasketId())
                .externalProductId(post.getExternalProductId())
                .body(post.getBody())
                .imageUrl(post.getImageUrl())
                .reported(Boolean.TRUE.equals(post.getReported()))
                .reactionCounts(counts)
                .viewerReaction(viewerReaction)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
