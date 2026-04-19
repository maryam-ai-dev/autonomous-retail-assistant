package com.aisleon.social;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(UUID id) {
        super("post not found: " + id);
    }
}
