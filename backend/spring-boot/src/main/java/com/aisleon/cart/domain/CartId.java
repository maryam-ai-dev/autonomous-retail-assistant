package com.aisleon.cart.domain;

import java.util.Objects;
import java.util.UUID;

public class CartId {

    private final UUID value;

    public CartId(UUID value) {
        Objects.requireNonNull(value, "CartId value must not be null");
        this.value = value;
    }

    public static CartId of(UUID value) {
        return new CartId(value);
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartId cartId = (CartId) o;
        return value.equals(cartId.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    @Override
    public String toString() { return value.toString(); }
}
