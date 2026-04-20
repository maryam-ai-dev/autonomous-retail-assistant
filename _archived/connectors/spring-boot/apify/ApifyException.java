package com.aisleon.scraping.apify;

public class ApifyException extends RuntimeException {
    private final boolean timeout;
    private final int statusCode;

    public ApifyException(String message, boolean timeout, int statusCode) {
        super(message);
        this.timeout = timeout;
        this.statusCode = statusCode;
    }

    public ApifyException(String message, Throwable cause) {
        super(message, cause);
        this.timeout = false;
        this.statusCode = 0;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
