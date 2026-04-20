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
@Schema(description = "User's taste profile — dietary, retailer, household, and clothing preferences")
public class TasteProfileResponse {

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @Schema(description = "Exclude HALAL_UNKNOWN meat products", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean halalOnly;

    @Schema(description = "Vegan-only basket", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean veganOnly;

    @Schema(description = "Vegetarian-only basket", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean vegetarianOnly;

    @Schema(description = "Gluten-free preference", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean glutenFree;

    @Schema(description = "Dairy-free preference", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean dairyFree;

    @Schema(description = "Prefer organic items", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean organicPreferred;

    @Schema(description = "Retailers the user explicitly allows", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> retailerAllowList;

    @Schema(description = "Retailers the user excludes", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> retailerDenyList;

    @Schema(description = "Preferred brands", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> preferredBrands;

    @Schema(description = "Household size (people)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer householdSize;

    @Schema(description = "Household has children", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasChildren;

    @Schema(description = "Household has pets", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasPets;

    @Schema(description = "Sensitive to scented cleaning products", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean cleaningScentSensitive;

    @Schema(description = "Top size (clothing) — null if not set", nullable = true)
    private String topSize;

    @Schema(description = "Bottom size (clothing) — null if not set", nullable = true)
    private String bottomSize;

    @Schema(description = "UK shoe size — null if not set", nullable = true)
    private BigDecimal shoeSizeUk;

    @Schema(description = "Dress size — null if not set", nullable = true)
    private String dressSize;

    @Schema(
            description =
                    "Size-match strategy for applySizeFilter: EXACT (default), SIZE_UP (also"
                            + " include one size up — looser fit), or SIZE_DOWN (fitted).",
            nullable = true)
    private String sizePreference;

    @Schema(description = "Additional clothing preferences", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> clothingPreferences;
}
