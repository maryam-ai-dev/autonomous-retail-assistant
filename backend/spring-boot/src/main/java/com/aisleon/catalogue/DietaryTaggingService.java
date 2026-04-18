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

    private static final EnumSet<ProductSubcategory> HALAL_LIKELY_SUBCATEGORIES =
            EnumSet.of(
                    ProductSubcategory.FISH_SEAFOOD,
                    ProductSubcategory.FRUIT_VEG,
                    ProductSubcategory.DAIRY,
                    ProductSubcategory.BAKERY);

    private static final String WARNING_HALAL_LIKELY =
            "halal status inferred, not certified";
    private static final String WARNING_HALAL_UNKNOWN =
            "halal status unknown — please verify before purchase";

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

    public HalalTaggingResult classifyHalal(
            String brand,
            ProductSubcategory subcategory,
            List<CertificationTag> certifications) {
        List<CertificationTag> certs =
                certifications == null ? List.of() : certifications;

        if (certs.contains(CertificationTag.HALAL_CERTIFIED)) {
            return new HalalTaggingResult(
                    Optional.of(DietaryTag.HALAL_VERIFIED), List.of());
        }

        boolean brandKnown = brand != null && !brand.isBlank()
                && knownHalalBrands.contains(brand.trim().toLowerCase());

        if (brandKnown) {
            return new HalalTaggingResult(
                    Optional.of(DietaryTag.HALAL_LIKELY),
                    List.of(WARNING_HALAL_LIKELY));
        }

        if (subcategory != null
                && HALAL_LIKELY_SUBCATEGORIES.contains(subcategory)) {
            return new HalalTaggingResult(
                    Optional.of(DietaryTag.HALAL_LIKELY),
                    List.of(WARNING_HALAL_LIKELY));
        }

        if (subcategory == ProductSubcategory.MEAT_POULTRY) {
            return new HalalTaggingResult(
                    Optional.of(DietaryTag.HALAL_UNKNOWN),
                    List.of(WARNING_HALAL_UNKNOWN));
        }

        return new HalalTaggingResult(Optional.empty(), List.of());
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
