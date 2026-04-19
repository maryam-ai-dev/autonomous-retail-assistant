package com.aisleon.basket;

import java.util.UUID;

public class FlagNotPresentException extends RuntimeException {
    public FlagNotPresentException(UUID itemId) {
        super("basket item " + itemId + " has no substitution flag to resolve");
    }
}
