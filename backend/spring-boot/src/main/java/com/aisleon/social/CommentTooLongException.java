package com.aisleon.social;

public class CommentTooLongException extends RuntimeException {

    private final int actualLength;

    public CommentTooLongException(int actualLength) {
        super("COMMENT_TOO_LONG: " + actualLength);
        this.actualLength = actualLength;
    }

    public int actualLength() {
        return actualLength;
    }
}
