package com.aisleon.checkout.infrastructure.executors;

import com.aisleon.cart.domain.Cart;

import java.util.UUID;

/**
 * Strategy interface for executing a checkout against a marketplace.
 */
public interface CheckoutExecutor {

    CheckoutResult execute(Cart cart, UUID userId);
}
