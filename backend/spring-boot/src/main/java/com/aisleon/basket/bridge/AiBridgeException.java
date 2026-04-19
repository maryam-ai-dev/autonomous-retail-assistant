package com.aisleon.basket.bridge;

public class AiBridgeException extends RuntimeException {
    public AiBridgeException(String message) {
        super(message);
    }

    public AiBridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
