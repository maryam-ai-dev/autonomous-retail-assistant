package com.aisleon.basket;

import java.util.UUID;

public class BasketItemNotFoundException extends RuntimeException {
    public BasketItemNotFoundException(UUID basketId, UUID itemId) {
        super("basket item " + itemId + " not found in basket " + basketId);
    }
}
