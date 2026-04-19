package com.aisleon.social.api;

import com.aisleon.social.domain.ReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Toggle a reaction on a post. Same type twice clears the reaction.")
public class ReactionRequest {

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private ReactionType reactionType;
}
