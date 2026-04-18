package com.aisleon.basket;

public record ClothingProfile(
        String topSize,
        String bottomSize,
        String shoeSize,
        String fit) {
    public boolean isComplete() {
        return notBlank(topSize) && notBlank(bottomSize) && notBlank(shoeSize);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
