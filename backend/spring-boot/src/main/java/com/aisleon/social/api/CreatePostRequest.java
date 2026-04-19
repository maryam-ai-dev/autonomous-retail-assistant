package com.aisleon.social.api;

import com.aisleon.social.domain.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create a new social post")
public class CreatePostRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private PostType postType;

    @Schema(nullable = true, description = "Required when postType = BASKET_SHARE")
    private UUID basketId;

    @Schema(nullable = true, description = "Required when postType = PRODUCT_REVIEW")
    private String externalProductId;

    @Size(max = 2000)
    @Schema(description = "Free-text body")
    private String body;

    @Schema(nullable = true)
    private String imageUrl;
}
