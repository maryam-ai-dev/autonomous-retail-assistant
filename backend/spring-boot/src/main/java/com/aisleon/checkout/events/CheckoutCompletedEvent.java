package com.aisleon.checkout.events;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutCompletedEvent(
        UUID userId,
        UUID orderId,
        String merchantOrderRef,
        BigDecimal totalAmount
) {}
