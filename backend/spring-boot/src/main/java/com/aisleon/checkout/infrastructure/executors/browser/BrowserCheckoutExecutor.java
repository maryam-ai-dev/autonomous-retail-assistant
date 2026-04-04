package com.aisleon.checkout.infrastructure.executors.browser;

import com.aisleon.cart.domain.Cart;
import com.aisleon.checkout.infrastructure.executors.CheckoutExecutor;
import com.aisleon.checkout.infrastructure.executors.CheckoutResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Browser-based checkout executor stub. Will use Playwright for browser automation.
 */
@Component
public class BrowserCheckoutExecutor implements CheckoutExecutor {

    @Override
    public CheckoutResult execute(Cart cart, UUID userId) {
        return new CheckoutResult(false, null, "BROWSER", "Browser checkout not yet implemented");
    }
}
