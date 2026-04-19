package com.aisleon.basket;

import java.util.UUID;

public class BasketNotFoundException extends RuntimeException {
    public BasketNotFoundException(UUID id) {
        super("basket not found: " + id);
    }
}
