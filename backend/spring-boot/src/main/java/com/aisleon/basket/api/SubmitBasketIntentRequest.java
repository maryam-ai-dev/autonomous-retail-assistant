package com.aisleon.basket.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Free-text basket intent — parsed server-side")
public class SubmitBasketIntentRequest {

    @NotBlank
    @Size(max = 1000)
    @Schema(
            description = "Raw user text e.g. 'weekly groceries under £70, halal'",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String text;
}
