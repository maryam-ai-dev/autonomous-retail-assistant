package com.aisleon.scraping;

import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.Retailer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCatalogueCache implements CatalogueCache {

    private final ConcurrentMap<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Entry> get(Retailer retailer, String query) {
        return Optional.ofNullable(store.get(cacheKey(retailer, query)));
    }

    @Override
    public void put(Retailer retailer, String query, List<NormalizedProduct> products) {
        store.put(
                cacheKey(retailer, query),
                new Entry(List.copyOf(products), Instant.now()));
    }
}
