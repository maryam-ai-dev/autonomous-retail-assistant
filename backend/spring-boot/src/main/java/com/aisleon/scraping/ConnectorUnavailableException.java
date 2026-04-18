package com.aisleon.scraping;

import com.aisleon.catalogue.Retailer;

public class ConnectorUnavailableException extends RuntimeException {
    private final Retailer retailer;
    private final String reason;

    public ConnectorUnavailableException(Retailer retailer, String reason) {
        super("Connector unavailable: " + retailer + " — " + reason);
        this.retailer = retailer;
        this.reason = reason;
    }

    public Retailer getRetailer() {
        return retailer;
    }

    public String getReason() {
        return reason;
    }
}
