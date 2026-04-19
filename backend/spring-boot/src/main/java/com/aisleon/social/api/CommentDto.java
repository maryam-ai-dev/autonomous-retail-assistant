package com.aisleon.social.api;

import com.aisleon.social.domain.CommentTarget;
import com.aisleon.social.infrastructure.CommentJpaEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A comment on a post or shared basket")
public class CommentDto {

    private UUID id;
    private CommentTarget targetType;
    private UUID targetId;
    private UUID authorUserId;
    private String body;
    private boolean reported;
    private LocalDateTime createdAt;

    public static CommentDto fromEntity(CommentJpaEntity e) {
        return CommentDto.builder()
                .id(e.getId())
                .targetType(e.getTargetType())
                .targetId(e.getTargetId())
                .authorUserId(e.getAuthorUserId())
                .body(e.getBody())
                .reported(Boolean.TRUE.equals(e.getReported()))
                .createdAt(e.getCreatedAt())
                .build();
    }
}
