package com.aisleon.basket;

import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BasketApprovedEvent(
        UUID basketId,
        UUID basketIntentId,
        UUID userId,
        BigDecimal totalCost,
        BigDecimal budget,
        List<Retailer> retailersUsed,
        Instant approvedAt) {}
