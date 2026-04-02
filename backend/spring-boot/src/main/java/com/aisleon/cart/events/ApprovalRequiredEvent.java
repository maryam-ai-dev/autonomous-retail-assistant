package com.aisleon.cart.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ApprovalRequiredEvent(
        UUID cartId,
        UUID userId,
        BigDecimal totalAmount,
        String triggerReason,
        List<String> warnings
) {
}
