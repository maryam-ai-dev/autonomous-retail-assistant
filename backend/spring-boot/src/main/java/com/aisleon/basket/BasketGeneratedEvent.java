package com.aisleon.basket;

import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BasketGeneratedEvent(
        UUID basketId,
        UUID basketIntentId,
        UUID userId,
        List<Retailer> retailersUsed,
        int itemCount,
        BigDecimal totalCost,
        BigDecimal budget,
        boolean trimmed,
        int trimmedItemCount) {}
