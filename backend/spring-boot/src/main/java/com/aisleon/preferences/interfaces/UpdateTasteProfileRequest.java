package com.aisleon.preferences.interfaces;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partial update to user's taste profile — null fields are left unchanged")
public class UpdateTasteProfileRequest {

    @Schema(nullable = true)
    private Boolean halalOnly;

    @Schema(nullable = true)
    private Boolean veganOnly;

    @Schema(nullable = true)
    private Boolean vegetarianOnly;

    @Schema(nullable = true)
    private Boolean glutenFree;

    @Schema(nullable = true)
    private Boolean dairyFree;

    @Schema(nullable = true)
    private Boolean organicPreferred;

    @Schema(nullable = true)
    private List<String> retailerAllowList;

    @Schema(nullable = true)
    private List<String> retailerDenyList;

    @Schema(nullable = true)
    private List<String> preferredBrands;

    @Schema(nullable = true)
    private Integer householdSize;

    @Schema(nullable = true)
    private Boolean hasChildren;

    @Schema(nullable = true)
    private Boolean hasPets;

    @Schema(nullable = true)
    private Boolean cleaningScentSensitive;

    @Schema(nullable = true)
    private String topSize;

    @Schema(nullable = true)
    private String bottomSize;

    @Schema(nullable = true)
    private BigDecimal shoeSizeUk;

    @Schema(nullable = true)
    private String dressSize;

    @Schema(
            nullable = true,
            description = "One of EXACT, SIZE_UP, SIZE_DOWN. Defaults to EXACT when null.")
    private String sizePreference;

    @Schema(nullable = true)
    private List<String> clothingPreferences;
}
