package com.aisleon.checkout.events;

import java.util.UUID;

public record CheckoutFailedEvent(
        UUID userId,
        UUID cartId,
        String reason
) {}
