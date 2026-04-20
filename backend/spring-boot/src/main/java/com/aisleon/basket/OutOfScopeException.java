package com.aisleon.basket;

/**
 * Thrown when a parsed basket intent is outside Aisleon's coverage (most
 * commonly grocery, which NourishOS handles). Translated to HTTP 422 with
 * {@code { reason: OUT_OF_SCOPE, detail: ... }} by {@code BasketExceptionHandler}.
 */
public class OutOfScopeException extends RuntimeException {

    private final String reason;

    public OutOfScopeException(String reason, String message) {
        super(message);
        this.reason = reason;
    }

    public String reason() {
        return reason;
    }
}
