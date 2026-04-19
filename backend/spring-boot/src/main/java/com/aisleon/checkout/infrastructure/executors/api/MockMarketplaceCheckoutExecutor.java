package com.aisleon.checkout.infrastructure.executors.api;

import com.aisleon.cart.domain.Cart;
import com.aisleon.checkout.infrastructure.executors.CheckoutExecutor;
import com.aisleon.checkout.infrastructure.executors.CheckoutResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock checkout executor. The current Aisleon flow hands off to retailer
 * checkout pages via {@code GET /api/baskets/{id}/checkout-links} (sprint
 * B7.3); this executor remains as a placeholder for any in-process checkout
 * flow we may add later.
 */
@Component
public class MockMarketplaceCheckoutExecutor implements CheckoutExecutor {

    private static final Logger log = LoggerFactory.getLogger(MockMarketplaceCheckoutExecutor.class);

    @Override
    public CheckoutResult execute(Cart cart, UUID userId) {
        log.info("Executing mock checkout for user {} — cart {} with {} items, total {}",
                userId, cart.getId(), cart.getItems().size(), cart.getTotalAmount());

        String merchantOrderRef = "MOCK-" + UUID.randomUUID();

        log.info("Mock checkout succeeded — order ref: {}", merchantOrderRef);

        return new CheckoutResult(true, merchantOrderRef, "API_MOCK", null);
    }
}
