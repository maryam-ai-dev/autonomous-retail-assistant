package com.aisleon.cart.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart aggregate root. Pure Java, no JPA or Spring imports.
 */
public class Cart {

    private final UUID id;
    private final UUID userId;
    private CartStatus status;
    private final List<CartItem> items;

    public Cart(UUID id, UUID userId, CartStatus status, List<CartItem> items) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.items = new ArrayList<>(items);
    }

    /**
     * Add an item to the cart. Rejects duplicates by externalProductId.
     */
    public void addItem(CartItem item) {
        boolean exists = items.stream()
                .anyMatch(i -> i.getExternalProductId().equals(item.getExternalProductId()));
        if (exists) {
            throw new CartItemAlreadyExistsException(item.getExternalProductId());
        }
        items.add(item);
    }

    public void removeItem(UUID itemId) {
        items.removeIf(i -> i.getId().equals(itemId));
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Getters

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public CartStatus getStatus() { return status; }
    public List<CartItem> getItems() { return List.copyOf(items); }

    public void setStatus(CartStatus status) { this.status = status; }
}
