package com.aisleon.preferences.interfaces;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Clothing-profile completeness gate")
public class ClothingProfileCompleteResponse {

    @Schema(description = "True iff top, bottom, shoe, and dress sizes are all set",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean complete;
}
