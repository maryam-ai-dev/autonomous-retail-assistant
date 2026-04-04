package com.aisleon.checkout.infrastructure.executors;

/**
 * Result of a checkout execution attempt.
 */
public record CheckoutResult(
        boolean success,
        String merchantOrderRef,
        String executorType,
        String errorMessage
) {}
