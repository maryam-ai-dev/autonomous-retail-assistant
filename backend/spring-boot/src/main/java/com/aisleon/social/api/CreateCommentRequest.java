package com.aisleon.social.api;

import com.aisleon.social.domain.CommentTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create a comment on a post or shared basket")
public class CreateCommentRequest {

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private CommentTarget targetType;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID targetId;

    @NotBlank
    @Size(max = 500)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    private String body;
}
