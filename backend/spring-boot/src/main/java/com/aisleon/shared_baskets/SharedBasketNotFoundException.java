package com.aisleon.shared_baskets;

public class SharedBasketNotFoundException extends RuntimeException {

    public SharedBasketNotFoundException(String shareId) {
        super("shared basket not found: " + shareId);
    }
}
