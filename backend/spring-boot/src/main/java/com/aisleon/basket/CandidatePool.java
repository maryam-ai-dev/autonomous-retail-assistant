package com.aisleon.basket;

import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.Retailer;
import java.util.List;
import java.util.Map;

/**
 * Output of {@link CandidateSelectionService}. A filtered, sorted, deduplicated
 * pool of NormalizedProduct candidates plus a per-retailer failure map for the
 * retailers that returned nothing (partial failure is not fatal).
 */
public record CandidatePool(
        List<NormalizedProduct> products,
        Map<Retailer, String> retailerFailures) {

    public CandidatePool {
        products = products == null ? List.of() : List.copyOf(products);
        retailerFailures = retailerFailures == null ? Map.of() : Map.copyOf(retailerFailures);
    }
}
