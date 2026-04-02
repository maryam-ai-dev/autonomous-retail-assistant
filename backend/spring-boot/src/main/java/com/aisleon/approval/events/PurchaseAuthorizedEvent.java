package com.aisleon.approval.events;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseAuthorizedEvent(
        UUID approvalId,
        UUID userId,
        UUID cartId,
        BigDecimal totalAmount
) {
}
