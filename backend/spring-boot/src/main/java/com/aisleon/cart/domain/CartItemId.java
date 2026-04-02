package com.aisleon.cart.domain;

import java.util.Objects;
import java.util.UUID;

public class CartItemId {

    private final UUID value;

    public CartItemId(UUID value) {
        Objects.requireNonNull(value, "CartItemId value must not be null");
        this.value = value;
    }

    public static CartItemId of(UUID value) {
        return new CartItemId(value);
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemId that = (CartItemId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    @Override
    public String toString() { return value.toString(); }
}
