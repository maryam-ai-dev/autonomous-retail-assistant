package com.aisleon.basket;

import com.aisleon.catalogue.DietaryTag;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BasketConstraintEngine {

    private static final EnumSet<ProductSubcategory> MEAT_SUBCATEGORIES =
            EnumSet.of(ProductSubcategory.MEAT_POULTRY);

    private static final EnumSet<ProductCategory> FASHION_CATEGORIES =
            EnumSet.of(ProductCategory.FASHION);

    public boolean exceedsBudget(List<BasketItem> items, BigDecimal budget) {
        return totalCost(items).compareTo(budget) > 0;
    }

    public BigDecimal totalCost(List<BasketItem> items) {
        return items.stream()
                .map(it -> it.product().price().multiply(BigDecimal.valueOf(it.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<NormalizedProduct> applyDietaryFilter(
            List<NormalizedProduct> candidates, TasteProfile profile) {
        if (profile == null) return candidates;
        List<NormalizedProduct> out = new ArrayList<>(candidates.size());
        for (NormalizedProduct p : candidates) {
            boolean isMeat = MEAT_SUBCATEGORIES.contains(p.subcategory());
            if (profile.halalOnly() && isMeat
                    && p.dietaryTags().contains(DietaryTag.HALAL_UNKNOWN)) {
                continue;
            }
            if (profile.veganOnly() && !p.dietaryTags().contains(DietaryTag.VEGAN)) {
                continue;
            }
            out.add(p);
        }
        return out;
    }

    /** Size-dependent fashion subcategories — all others pass the size filter untouched. */
    private static final EnumSet<ProductSubcategory> SIZE_DEPENDENT_SUBCATEGORIES =
            EnumSet.of(
                    ProductSubcategory.TOPS,
                    ProductSubcategory.BOTTOMS,
                    ProductSubcategory.DRESSES,
                    ProductSubcategory.OUTERWEAR,
                    ProductSubcategory.FOOTWEAR,
                    ProductSubcategory.SPORTSWEAR,
                    ProductSubcategory.UNDERWEAR);

    public List<NormalizedProduct> applySizeFilter(
            List<NormalizedProduct> candidates, ClothingProfile clothing) {
        if (clothing == null || !clothing.hasAnySize()) return candidates;
        List<NormalizedProduct> out = new ArrayList<>(candidates.size());
        for (NormalizedProduct p : candidates) {
            if (!FASHION_CATEGORIES.contains(p.category())) {
                out.add(p);
                continue;
            }
            if (!SIZE_DEPENDENT_SUBCATEGORIES.contains(p.subcategory())) {
                // Accessories, bags, jewellery — not size-dependent.
                out.add(p);
                continue;
            }
            String desired = sizeFieldFor(p.subcategory(), clothing);
            if (desired == null || desired.isBlank()) {
                // No size set for this subcategory — do not filter.
                out.add(p);
                continue;
            }
            if (p.sizeText() == null || p.sizeText().isBlank()) {
                // Size unknown on the product — pass through rather than exclude.
                out.add(p);
                continue;
            }
            if (productAdvertisesSize(p.sizeText(), desired, clothing.sizePreference())) {
                out.add(p);
            }
        }
        return out;
    }

    private static String sizeFieldFor(ProductSubcategory subcategory, ClothingProfile clothing) {
        return switch (subcategory) {
            case FOOTWEAR -> clothing.shoeSize();
            case BOTTOMS -> clothing.bottomSize();
            case DRESSES -> clothing.dressSize();
            default -> clothing.topSize();
        };
    }

    private static boolean productAdvertisesSize(
            String sizeText, String desired, ClothingProfile.SizePreference preference) {
        Set<String> available = tokenizeSizes(sizeText);
        if (available.contains(desired.toUpperCase())) return true;
        if (preference == ClothingProfile.SizePreference.SIZE_UP) {
            String up = neighbourSize(desired, 1);
            if (up != null && available.contains(up.toUpperCase())) return true;
        } else if (preference == ClothingProfile.SizePreference.SIZE_DOWN) {
            String down = neighbourSize(desired, -1);
            if (down != null && available.contains(down.toUpperCase())) return true;
        }
        return false;
    }

    private static Set<String> tokenizeSizes(String sizeText) {
        Set<String> out = new HashSet<>();
        for (String raw : sizeText.split("[,/|]+")) {
            String token = raw.trim().toUpperCase();
            if (!token.isEmpty()) out.add(token);
        }
        return out;
    }

    /**
     * Lettered sizes (XS/S/M/L/XL/XXL) and numeric sizes are both in scope. For
     * numeric sizes we nudge by the UK-step convention: 2 for clothing (8→10),
     * 0.5 for shoes (6→6.5). We return {@code null} when we can't confidently
     * compute a neighbour.
     */
    private static String neighbourSize(String desired, int direction) {
        String u = desired.trim().toUpperCase();
        List<String> lettered = List.of("XS", "S", "M", "L", "XL", "XXL");
        int idx = lettered.indexOf(u);
        if (idx >= 0) {
            int target = idx + direction;
            if (target < 0 || target >= lettered.size()) return null;
            return lettered.get(target);
        }
        try {
            if (u.contains(".")) {
                double val = Double.parseDouble(u) + direction * 0.5;
                // Normalise "10.0" → "10"
                String s = String.valueOf(val);
                return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
            }
            int val = Integer.parseInt(u);
            return String.valueOf(val + direction * 2);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public List<NormalizedProduct> applyRetailerFilter(
            List<NormalizedProduct> candidates, TasteProfile profile) {
        if (profile == null) return candidates;
        Set<Retailer> deny = new HashSet<>(profile.retailerDenyList());
        Set<Retailer> allow = new HashSet<>(profile.retailerAllowList());
        List<NormalizedProduct> out = new ArrayList<>(candidates.size());
        for (NormalizedProduct p : candidates) {
            if (deny.contains(p.retailer())) continue;
            if (!allow.isEmpty() && !allow.contains(p.retailer())) continue;
            out.add(p);
        }
        return out;
    }

    public List<SubstitutionFlag> detectSubstitutions(
            List<BasketItem> newItems, List<BasketItem> previousItems) {
        List<SubstitutionFlag> flags = new ArrayList<>();
        if (previousItems == null || previousItems.isEmpty()) return flags;
        for (BasketItem prev : previousItems) {
            for (BasketItem current : newItems) {
                if (sameCanonical(prev, current)
                        && !sameBrand(prev, current)) {
                    flags.add(new SubstitutionFlag(
                            "BRAND_CHANGED",
                            String.format(
                                    "Brand changed from %s to %s",
                                    prev.product().brand(),
                                    current.product().brand())));
                }
            }
        }
        return flags;
    }

    public boolean categoryViolation(NormalizedProduct product, ParsedIntent intent) {
        if (intent == null || intent.primaryCategory() == null) return false;
        // Mixed-category intents carry the GROCERY primary flag with additional
        // tags; we only flag a violation if the product is clearly outside the
        // requested category and there is no 'mixed' tag indicating otherwise.
        if (intent.primaryCategory() == ProductCategory.GROCERY
                && product.category() == ProductCategory.FASHION
                && !intent.tags().contains("mixed")) {
            return true;
        }
        return false;
    }

    public List<NormalizedProduct> deduplicate(List<NormalizedProduct> candidates) {
        Set<String> seen = new HashSet<>();
        List<NormalizedProduct> out = new ArrayList<>(candidates.size());
        for (NormalizedProduct p : candidates) {
            String key = p.retailer() + ":" + p.externalId();
            if (seen.add(key)) out.add(p);
        }
        return out;
    }

    public List<BasketItem> trimToBudget(List<BasketItem> items, BigDecimal budget) {
        if (budget == null) return items;
        List<BasketItem> remaining = new ArrayList<>(items);
        while (exceedsBudget(remaining, budget)) {
            if (remaining.isEmpty()) {
                throw new BudgetTooLowException(budget);
            }
            BasketItem victim = pickTrimVictim(remaining);
            if (victim == null) {
                throw new BudgetTooLowException(budget);
            }
            remaining.remove(victim);
        }
        if (remaining.isEmpty()) {
            throw new BudgetTooLowException(budget);
        }
        return remaining;
    }

    private BasketItem pickTrimVictim(List<BasketItem> items) {
        // Prefer trimming items where we have low confidence or where
        // the halal tag is HALAL_UNKNOWN or HALAL_LIKELY; keep HALAL_VERIFIED
        // items last. De-prioritise items that already have an unresolved
        // substitution flag that the user accepted (resolved=true).
        return items.stream()
                .min(Comparator
                        .comparingInt(BasketConstraintEngine::trimPriority)
                        .thenComparingDouble(BasketConstraintEngine::priceConfidenceRatio))
                .orElse(null);
    }

    /**
     * Lower score → trimmed first.
     * HALAL_VERIFIED → highest score (last to trim)
     * HALAL_LIKELY   → mid
     * HALAL_UNKNOWN / no halal tag → lowest (trim first)
     */
    private static int trimPriority(BasketItem item) {
        NormalizedProduct p = item.product();
        int base;
        if (p.dietaryTags().contains(DietaryTag.HALAL_VERIFIED)) base = 3;
        else if (p.dietaryTags().contains(DietaryTag.HALAL_LIKELY)) base = 2;
        else base = 1;
        // User accepted substitution → deprioritise for trimming
        if (item.substitutionFlag().isPresent()
                && item.substitutionFlag().get().resolved()) {
            base += 2;
        }
        return base;
    }

    private static double priceConfidenceRatio(BasketItem item) {
        NormalizedProduct p = item.product();
        double confidence = Math.max(0.01, p.confidenceScore());
        double itemCost = p.price().doubleValue() * item.quantity();
        return itemCost / confidence;
    }

    private static boolean sameCanonical(BasketItem a, BasketItem b) {
        String ca = a.product().canonicalName();
        String cb = b.product().canonicalName();
        return ca != null && ca.equals(cb);
    }

    private static boolean sameBrand(BasketItem a, BasketItem b) {
        String ba = a.product().brand();
        String bb = b.product().brand();
        if (ba == null || bb == null) return ba == bb;
        return ba.equalsIgnoreCase(bb);
    }
}
