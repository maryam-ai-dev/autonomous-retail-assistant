package com.aisleon.basket;

public record ClothingProfile(
        String topSize,
        String bottomSize,
        String shoeSize,
        String dressSize,
        SizePreference sizePreference) {

    public ClothingProfile {
        if (sizePreference == null) sizePreference = SizePreference.EXACT;
    }

    /** Back-compat four-arg constructor — defaults sizePreference to EXACT. */
    public ClothingProfile(String topSize, String bottomSize, String shoeSize, String dressSize) {
        this(topSize, bottomSize, shoeSize, dressSize, SizePreference.EXACT);
    }

    /**
     * B12.3: a profile is "complete enough" once at least one size field is
     * populated. The size filter decides, per subcategory, whether it has a
     * usable size; missing fields simply skip filtering for that subcategory.
     */
    public boolean isComplete() {
        return notBlank(topSize) || notBlank(bottomSize) || notBlank(shoeSize) || notBlank(dressSize);
    }

    public boolean hasAnySize() {
        return isComplete();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public enum SizePreference {
        EXACT,
        SIZE_UP,
        SIZE_DOWN;

        public static SizePreference parse(String raw) {
            if (raw == null) return EXACT;
            try {
                return SizePreference.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return EXACT;
            }
        }
    }
}
