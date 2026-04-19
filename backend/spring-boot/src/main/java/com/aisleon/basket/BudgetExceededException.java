package com.aisleon.basket;

import java.math.BigDecimal;

public class BudgetExceededException extends RuntimeException {

    private final BigDecimal totalCost;
    private final BigDecimal budget;

    public BudgetExceededException(BigDecimal totalCost, BigDecimal budget) {
        super("basket total " + totalCost + " exceeds budget " + budget);
        this.totalCost = totalCost;
        this.budget = budget;
    }

    public BigDecimal totalCost() {
        return totalCost;
    }

    public BigDecimal budget() {
        return budget;
    }
}
