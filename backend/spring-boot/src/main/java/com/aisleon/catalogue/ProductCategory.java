package com.aisleon.catalogue;

public enum ProductCategory {
    /**
     * Deprecated in v1.1. Grocery is out of Aisleon scope — NourishOS handles
     * grocery. Retained as an enum value so pre-B12 data still deserialises,
     * but never surfaced via the API (filtered in candidate selection).
     */
    @Deprecated
    GROCERY,
    HEALTH_BEAUTY,
    GENERAL_MERCHANDISE,
    FASHION,
    ELECTRONICS,
    PRICE_REFERENCE_ONLY,
    UNKNOWN
}
