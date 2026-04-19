package com.aisleon.basket;

public class UnresolvedFlagsException extends RuntimeException {

    private final int unresolvedCount;

    public UnresolvedFlagsException(int unresolvedCount) {
        super("basket has " + unresolvedCount + " unresolved substitution flags");
        this.unresolvedCount = unresolvedCount;
    }

    public int unresolvedCount() {
        return unresolvedCount;
    }
}
