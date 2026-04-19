package com.aisleon.basket;

import com.aisleon.catalogue.ProductCategory;
import com.aisleon.preferences.application.TasteProfileService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Gate that blocks FASHION basket intents when the user's clothing profile
 * has missing sizes. GROCERY (and every other category) passes through.
 *
 * <p>Throws {@link ClothingProfileIncompleteException} — handled by
 * {@link BasketExceptionHandler} as 428 Precondition Required.
 * Logged at INFO because this is an expected flow, not an error.
 */
@Service
public class ClothingProfileGate {

    private static final Logger log = LoggerFactory.getLogger(ClothingProfileGate.class);

    private final TasteProfileService tasteProfileService;

    public ClothingProfileGate(TasteProfileService tasteProfileService) {
        this.tasteProfileService = tasteProfileService;
    }

    public void assertReadyFor(ParsedIntent intent, UUID userId) {
        if (intent == null || intent.primaryCategory() != ProductCategory.FASHION) {
            return;
        }
        boolean complete = tasteProfileService.clothingComplete(userId).isComplete();
        if (!complete) {
            log.info("clothing profile gate fired: user={} intent_category=FASHION", userId);
            throw new ClothingProfileIncompleteException();
        }
    }
}
