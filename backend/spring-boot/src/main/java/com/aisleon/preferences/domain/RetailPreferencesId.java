package com.aisleon.preferences.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object wrapping the UUID identifier for RetailPreferences.
 */
public class RetailPreferencesId {

    private final UUID value;

    public RetailPreferencesId(UUID value) {
        Objects.requireNonNull(value, "RetailPreferencesId value must not be null");
        this.value = value;
    }

    public static RetailPreferencesId of(UUID value) {
        return new RetailPreferencesId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetailPreferencesId that = (RetailPreferencesId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
