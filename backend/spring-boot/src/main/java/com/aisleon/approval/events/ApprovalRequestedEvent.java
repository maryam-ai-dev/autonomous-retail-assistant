package com.aisleon.approval.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ApprovalRequestedEvent(
        UUID approvalId,
        UUID userId,
        UUID cartId,
        String triggerReason,
        BigDecimal totalAmount
) {
}
