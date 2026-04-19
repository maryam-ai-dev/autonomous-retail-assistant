package com.aisleon.budget.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description =
                "Aggregated spend summary for one calendar month, derived from BASKET_APPROVED"
                        + " audit events for the calling user.")
public class BudgetSummary {

    @Schema(description = "ISO month (YYYY-MM).")
    private YearMonth month;

    @Schema(description = "Sum of approved basket totals in this month.")
    private BigDecimal spent;

    @Schema(description = "Sum of basket-intent budgets across approved baskets in this month.")
    private BigDecimal budget;

    @Schema(
            description =
                    "Estimated savings from offer prices vs full price. Currently 0 — basket"
                            + " items do not yet persist offer-flag metadata; populated when"
                            + " offers are tracked per item.")
    private BigDecimal savedVsFullPrice;

    @Schema(
            description =
                    "Spend per retailer. Each approved basket contributes totalCost split"
                            + " evenly across its retailersUsed.")
    private Map<String, BigDecimal> byRetailer;

    @Schema(description = "Number of approved baskets in this month.")
    private int basketCount;
}
