package com.aisleon.cart.events;

import java.math.BigDecimal;
import java.util.UUID;

public record CartCheckedOutEvent(
        UUID cartId,
        UUID userId,
        BigDecimal totalAmount,
        int itemCount
) {
}
