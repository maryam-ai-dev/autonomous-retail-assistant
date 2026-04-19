package com.aisleon.social;

public class SelfFollowException extends RuntimeException {
    public SelfFollowException() {
        super("SELF_FOLLOW_NOT_ALLOWED");
    }
}
