package com.aisleon.scraping;

import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.Retailer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CatalogueCache {

    record Entry(List<NormalizedProduct> products, Instant storedAt) {}

    Optional<Entry> get(Retailer retailer, String query);

    void put(Retailer retailer, String query, List<NormalizedProduct> products);

    default String cacheKey(Retailer retailer, String query) {
        return "catalogue:" + retailer.name() + ":" + query;
    }
}
