package com.aisleon.basket;

import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.Retailer;
import com.aisleon.scraping.CatalogueService;
import com.aisleon.scraping.ConnectorRegistry;
import com.aisleon.scraping.ConnectorStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Assembles a CandidatePool from across every available retailer, applies the
 * dietary/size/retailer filters via {@link BasketConstraintEngine}, dedupes,
 * sorts by confidenceScore, and returns the top {@value #MAX_PER_CATEGORY}
 * candidates per product category.
 *
 * <p>Partial retailer failure is logged but never fatal. If every available
 * retailer returns nothing, {@link NoRetailersAvailableException} is thrown
 * with the per-retailer failure reasons, so the caller can distinguish this
 * from a legitimate empty result.
 */
@Service
public class CandidateSelectionService {

    public static final int MAX_PER_CATEGORY = 50;

    private static final Logger log = LoggerFactory.getLogger(CandidateSelectionService.class);

    private final CatalogueService catalogueService;
    private final ConnectorRegistry registry;
    private final BasketConstraintEngine engine;

    public CandidateSelectionService(
            CatalogueService catalogueService,
            ConnectorRegistry registry,
            BasketConstraintEngine engine) {
        this.catalogueService = catalogueService;
        this.registry = registry;
        this.engine = engine;
    }

    public CandidatePool select(
            ParsedIntent intent,
            TasteProfile taste,
            ClothingProfile clothing,
            int maxResultsPerRetailer) {
        Set<Retailer> available = registry.availableRetailers();
        if (available.isEmpty()) {
            Map<Retailer, String> allFailures = collectKnownFailures();
            log.warn("Candidate selection aborted — no available retailers");
            throw new NoRetailersAvailableException(allFailures);
        }

        String query = deriveQuery(intent);
        Map<Retailer, List<NormalizedProduct>> byRetailer =
                catalogueService.searchAll(query, available, maxResultsPerRetailer);

        Map<Retailer, String> failures = new HashMap<>();
        List<NormalizedProduct> combined = new ArrayList<>();
        for (Map.Entry<Retailer, List<NormalizedProduct>> entry : byRetailer.entrySet()) {
            List<NormalizedProduct> products = entry.getValue();
            if (products == null || products.isEmpty()) {
                String reason = lastFailureReason(entry.getKey());
                failures.put(entry.getKey(), reason);
                log.info(
                        "Candidate selection partial failure: retailer={} reason={}",
                        entry.getKey(),
                        reason);
                continue;
            }
            combined.addAll(products);
        }

        if (combined.isEmpty()) {
            log.warn("Candidate selection — every available retailer returned empty");
            throw new NoRetailersAvailableException(failures);
        }

        combined = excludeGrocery(combined);

        List<NormalizedProduct> filtered = engine.applyDietaryFilter(combined, taste);
        if (intent != null && intent.primaryCategory() == ProductCategory.FASHION) {
            filtered = engine.applySizeFilter(filtered, clothing);
        }
        // Deny list is a hard filter; allow list is a soft preference at
        // candidate-selection time (used by the ranking comparator below).
        filtered = engine.applyRetailerFilter(filtered, denyOnly(taste));
        filtered = engine.deduplicate(filtered);

        List<NormalizedProduct> sorted = new ArrayList<>(filtered);
        sorted.sort(rankingComparator(taste));

        return new CandidatePool(topPerCategory(sorted), failures);
    }

    private List<NormalizedProduct> excludeGrocery(List<NormalizedProduct> candidates) {
        List<NormalizedProduct> out = new ArrayList<>(candidates.size());
        for (NormalizedProduct p : candidates) {
            if (p.category() == ProductCategory.GROCERY) {
                log.warn(
                        "GroceryCategoryExcluded: productId={} retailer={} — grocery out of scope",
                        p.externalId(),
                        p.retailer());
                continue;
            }
            out.add(p);
        }
        return out;
    }

    private static TasteProfile denyOnly(TasteProfile taste) {
        if (taste == null) return null;
        return new TasteProfile(
                taste.halalOnly(),
                taste.veganOnly(),
                taste.vegetarianOnly(),
                List.of(),
                taste.retailerDenyList(),
                taste.preferredBrands());
    }

    private static String deriveQuery(ParsedIntent intent) {
        if (intent == null) return "";
        if (intent.tags() != null && !intent.tags().isEmpty()) {
            return String.join(" ", intent.tags());
        }
        return intent.rawText() == null ? "" : intent.rawText();
    }

    private Comparator<NormalizedProduct> rankingComparator(TasteProfile taste) {
        Comparator<NormalizedProduct> allowListFirst =
                Comparator.comparingInt(
                        p ->
                                taste != null
                                                && !taste.retailerAllowList().isEmpty()
                                                && taste.retailerAllowList().contains(p.retailer())
                                        ? 0
                                        : 1);
        Comparator<NormalizedProduct> byConfidence =
                Comparator.comparingDouble(NormalizedProduct::confidenceScore).reversed();
        return allowListFirst.thenComparing(byConfidence);
    }

    private List<NormalizedProduct> topPerCategory(List<NormalizedProduct> sorted) {
        Map<ProductCategory, List<NormalizedProduct>> bucketed =
                new EnumMap<>(ProductCategory.class);
        for (NormalizedProduct p : sorted) {
            bucketed.computeIfAbsent(p.category(), k -> new ArrayList<>()).add(p);
        }
        List<NormalizedProduct> out = new ArrayList<>();
        for (List<NormalizedProduct> bucket : bucketed.values()) {
            int take = Math.min(MAX_PER_CATEGORY, bucket.size());
            out.addAll(bucket.subList(0, take));
        }
        return out;
    }

    private String lastFailureReason(Retailer retailer) {
        return registry
                .statusFor(retailer)
                .map(ConnectorStatus::lastFailureReason)
                .orElse("no result");
    }

    private Map<Retailer, String> collectKnownFailures() {
        Map<Retailer, String> out = new LinkedHashMap<>();
        for (ConnectorStatus status : registry.allStatuses()) {
            String reason =
                    status.lastFailureReason() != null
                            ? status.lastFailureReason()
                            : (status.disabled()
                                    ? "disabled by configuration"
                                    : "unhealthy");
            out.put(status.retailer(), reason);
        }
        return out;
    }
}
