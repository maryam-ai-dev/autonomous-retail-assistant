package com.aisleon.cart.events;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemAddedEvent(
        UUID cartId,
        UUID userId,
        String externalProductId,
        String title,
        BigDecimal price,
        String merchantName
) {
}
