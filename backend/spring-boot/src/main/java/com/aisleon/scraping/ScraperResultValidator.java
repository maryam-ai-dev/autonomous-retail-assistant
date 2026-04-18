package com.aisleon.scraping;

import com.aisleon.catalogue.NormalizedProduct;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScraperResultValidator {

    public enum Outcome {
        ACCEPT,
        REJECT_EMPTY,
        REJECT_TOO_FEW,
        REJECT_LOW_CONFIDENCE,
        REJECT_MISSING_IMAGES,
        REJECT_NULL_PRICE
    }

    public Outcome validate(List<NormalizedProduct> products) {
        if (products == null || products.isEmpty()) return Outcome.REJECT_EMPTY;
        if (products.size() < 2) return Outcome.REJECT_TOO_FEW;
        long nullPrice = products.stream()
                .filter(p -> p.price() == null
                        || p.price().signum() == 0)
                .count();
        if (nullPrice > 0) return Outcome.REJECT_NULL_PRICE;
        long lowConfidence = products.stream()
                .filter(p -> p.confidenceScore() < 0.3)
                .count();
        if ((double) lowConfidence / products.size() > 0.60) {
            return Outcome.REJECT_LOW_CONFIDENCE;
        }
        long missingImages = products.stream()
                .filter(p -> p.imageUrl() == null || p.imageUrl().isBlank())
                .count();
        if ((double) missingImages / products.size() > 0.50) {
            return Outcome.REJECT_MISSING_IMAGES;
        }
        return Outcome.ACCEPT;
    }
}
