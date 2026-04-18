package com.aisleon.basket;

public record SubstitutionFlag(
        String reason,
        String message,
        boolean resolved) {
    public SubstitutionFlag(String reason, String message) {
        this(reason, message, false);
    }
}
