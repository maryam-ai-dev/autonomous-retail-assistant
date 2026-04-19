package com.aisleon.basket;

public class BudgetTooLowForIntentException extends RuntimeException {
    public BudgetTooLowForIntentException(String detail) {
        super(detail);
    }
}
