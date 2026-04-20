package com.aisleon.catalogue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DietaryTaggingService {

    static final String HALAL_BRANDS_RESOURCE = "known_halal_brands.txt";

    /**
     * Sprint B12.4: non-food halal coverage. Only HEALTH_BEAUTY subcategories
     * and VITAMINS_SUPPLEMENTS attract halal tags by default. FASHION,
     * ELECTRONICS, GENERAL_MERCHANDISE: no halal status — never applicable to
     * non-consumable/non-applied products.
     */
    private static final EnumSet<ProductSubcategory> HALAL_SCOPE_SUBCATEGORIES =
            EnumSet.of(
                    ProductSubcategory.SKINCARE,
                    ProductSubcategory.HAIRCARE,
                    ProductSubcategory.DENTAL,
                    ProductSubcategory.VITAMINS_SUPPLEMENTS,
                    ProductSubcategory.PHARMACY,
                    ProductSubcategory.FRAGRANCE,
                    ProductSubcategory.MAKEUP);

    private static final EnumSet<ProductCategory> NEVER_HALAL_CATEGORIES =
            EnumSet.of(
                    ProductCategory.FASHION,
                    ProductCategory.ELECTRONICS,
                    ProductCategory.GENERAL_MERCHANDISE);

    private static final String WARNING_HALAL_LIKELY_BRAND =
            "halal status inferred from brand — please verify";
    private static final String WARNING_HALAL_UNKNOWN_NONFOOD =
            "halal status unknown — check ingredients for alcohol, porcine"
                    + " derivatives, or animal-derived components before purchasing.";

    private final Set<String> knownHalalBrands;

    public DietaryTaggingService() {
        this.knownHalalBrands = loadKnownHalalBrands();
    }

    public record HalalTaggingResult(
            Optional<DietaryTag> halalTag,
            List<String> warnings) {
        public HalalTaggingResult {
            if (warnings == null) warnings = List.of();
        }
    }

    /**
     * Primary entry point as of B12.4 — category-aware. See overload below for
     * the pre-B12.4 call sites still in service.
     */
    public HalalTaggingResult classifyHalal(
            String brand,
            ProductCategory category,
            ProductSubcategory subcategory,
            List<CertificationTag> certifications) {
        List<CertificationTag> certs =
                certifications == null ? List.of() : certifications;

        if (category != null && NEVER_HALAL_CATEGORIES.contains(category)) {
            return new HalalTaggingResult(Optional.empty(), List.of());
        }

        if (certs.contains(CertificationTag.HALAL_CERTIFIED)) {
            return new HalalTaggingResult(
                    Optional.of(DietaryTag.HALAL_VERIFIED), List.of());
        }

        boolean brandKnown = brand != null && !brand.isBlank()
                && knownHalalBrands.contains(brand.trim().toLowerCase());

        if (brandKnown) {
            return new HalalTaggingResult(
                    Optional.of(DietaryTag.HALAL_LIKELY),
                    List.of(WARNING_HALAL_LIKELY_BRAND));
        }

        if (subcategory != null && HALAL_SCOPE_SUBCATEGORIES.contains(subcategory)) {
            return new HalalTaggingResult(
                    Optional.of(DietaryTag.HALAL_UNKNOWN),
                    List.of(WARNING_HALAL_UNKNOWN_NONFOOD));
        }

        return new HalalTaggingResult(Optional.empty(), List.of());
    }

    /**
     * Back-compat overload — kept until all call sites thread through
     * ProductCategory. Infers category as null (no category gate).
     *
     * @deprecated use the four-arg overload with {@link ProductCategory}.
     */
    @Deprecated
    public HalalTaggingResult classifyHalal(
            String brand,
            ProductSubcategory subcategory,
            List<CertificationTag> certifications) {
        return classifyHalal(brand, null, subcategory, certifications);
    }

    private static Set<String> loadKnownHalalBrands() {
        Set<String> brands = new HashSet<>();
        ClassLoader cl = DietaryTaggingService.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(HALAL_BRANDS_RESOURCE)) {
            if (in == null) return Collections.unmodifiableSet(brands);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                    brands.add(trimmed.toLowerCase());
                }
            }
        } catch (IOException e) {
            // Return whatever was read before the failure.
            return Collections.unmodifiableSet(brands);
        }
        return Collections.unmodifiableSet(new HashSet<>(new ArrayList<>(brands)));
    }
}
