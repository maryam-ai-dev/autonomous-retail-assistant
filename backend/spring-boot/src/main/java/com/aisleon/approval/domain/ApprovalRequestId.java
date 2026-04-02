package com.aisleon.approval.domain;

import java.util.Objects;
import java.util.UUID;

public class ApprovalRequestId {

    private final UUID value;

    public ApprovalRequestId(UUID value) {
        Objects.requireNonNull(value, "ApprovalRequestId value must not be null");
        this.value = value;
    }

    public static ApprovalRequestId of(UUID value) {
        return new ApprovalRequestId(value);
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalRequestId that = (ApprovalRequestId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    @Override
    public String toString() { return value.toString(); }
}
