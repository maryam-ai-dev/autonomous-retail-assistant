package com.aisleon.scraping;

import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import java.util.List;

public interface RetailerConnector {

    Retailer getRetailer();

    List<RawScraperProduct> search(String query, int maxResults);

    boolean isHealthy();

    ConnectorStatus getStatus();
}
