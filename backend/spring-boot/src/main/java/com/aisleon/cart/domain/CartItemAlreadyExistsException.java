package com.aisleon.cart.domain;

public class CartItemAlreadyExistsException extends RuntimeException {

    public CartItemAlreadyExistsException(String externalProductId) {
        super("Product already exists in cart: " + externalProductId);
    }
}
