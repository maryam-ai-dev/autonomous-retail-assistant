package com.aisleon.basket;

import com.aisleon.catalogue.Retailer;
import java.util.Map;

public class NoRetailersAvailableException extends RuntimeException {

    private final Map<Retailer, String> failureReasons;

    public NoRetailersAvailableException(Map<Retailer, String> failureReasons) {
        super("no retailers available: " + failureReasons);
        this.failureReasons = Map.copyOf(failureReasons);
    }

    public Map<Retailer, String> failureReasons() {
        return failureReasons;
    }
}
