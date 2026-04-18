package com.aisleon.basket;

import java.math.BigDecimal;

public class BudgetTooLowException extends RuntimeException {
    private final BigDecimal budget;

    public BudgetTooLowException(BigDecimal budget) {
        super("Budget too low: " + budget);
        this.budget = budget;
    }

    public BigDecimal getBudget() {
        return budget;
    }
}
