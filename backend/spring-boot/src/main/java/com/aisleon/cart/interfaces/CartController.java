package com.aisleon.cart.interfaces;

import com.aisleon.cart.application.CartService;
import com.aisleon.cart.domain.Cart;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(
            Authentication authentication,
            @Validated @RequestBody AddCartItemRequest request) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Cart> removeItem(
            Authentication authentication,
            @PathVariable UUID itemId) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(cartService.checkoutCart(userId));
    }
}
