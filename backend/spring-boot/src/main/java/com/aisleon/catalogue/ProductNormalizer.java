package com.aisleon.catalogue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ProductNormalizer {

    private static final Set<String> TRACKING_PARAM_PREFIXES = Set.of(
            "utm_", "mc_", "ga_", "hsCtaTracking");
    private static final Set<String> TRACKING_PARAM_EXACT = Set.of(
            "gclid", "fbclid", "msclkid", "dclid", "wickedid",
            "affid", "affiliate", "ref", "refsrc", "srsltid",
            "yclid", "igshid", "mkevt", "mkcid");
    private static final Pattern SIZE_TOKEN_PATTERN =
            Pattern.compile(
                    "\\b\\d+(?:\\.\\d+)?\\s*(?:g|kg|ml|l|cl|oz|lb|pk|pack|x|ct|each|m|cm|mm)\\b",
                    Pattern.CASE_INSENSITIVE);

    private final DietaryTaggingService dietaryTaggingService;

    public ProductNormalizer(DietaryTaggingService dietaryTaggingService) {
        this.dietaryTaggingService = dietaryTaggingService;
    }

    public NormalizedProduct normalize(RawScraperProduct raw, Retailer retailer) {
        List<String> warnings = new ArrayList<>();
        List<DietaryTag> dietaryTags = new ArrayList<>();

        var halal = dietaryTaggingService.classifyHalal(
                raw.brand(), raw.category(), raw.subcategory(), raw.certificationTags());
        halal.halalTag().ifPresent(dietaryTags::add);
        warnings.addAll(halal.warnings());

        String canonicalName = canonicalize(raw.displayName());
        String cleanedUrl = stripTrackingParams(raw.productUrl());

        double confidence = scoreConfidence(raw, dietaryTags);

        return new NormalizedProduct(
                raw.externalId(),
                canonicalName,
                raw.displayName(),
                raw.brand(),
                retailer,
                raw.category(),
                raw.subcategory(),
                raw.price(),
                raw.unitPrice(),
                raw.unitBasis(),
                raw.sizeText(),
                raw.imageUrl(),
                cleanedUrl,
                raw.isAvailable(),
                raw.isBasketable(),
                List.copyOf(dietaryTags),
                raw.certificationTags(),
                raw.offerFlags(),
                confidence,
                raw.sourceFetchedAt(),
                List.copyOf(warnings),
                List.of());
    }

    static String canonicalize(String displayName) {
        if (displayName == null) return "";
        String cleaned = SIZE_TOKEN_PATTERN.matcher(displayName).replaceAll(" ");
        cleaned = cleaned.replaceAll("[\\p{Punct}]", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned.toLowerCase(Locale.UK);
    }

    static String stripTrackingParams(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return rawUrl;
        URI uri;
        try {
            uri = new URI(rawUrl);
        } catch (URISyntaxException e) {
            return rawUrl;
        }
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) return rawUrl;
        String[] parts = query.split("&");
        List<String> kept = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) continue;
            String key = part.split("=", 2)[0].toLowerCase(Locale.ROOT);
            if (TRACKING_PARAM_EXACT.contains(key)) continue;
            if (TRACKING_PARAM_PREFIXES.stream().anyMatch(key::startsWith)) continue;
            kept.add(part);
        }
        String newQuery = String.join("&", kept);
        try {
            URI out = new URI(
                    uri.getScheme(),
                    uri.getRawAuthority(),
                    uri.getRawPath(),
                    newQuery.isEmpty() ? null : newQuery,
                    uri.getRawFragment());
            return out.toString();
        } catch (URISyntaxException e) {
            return rawUrl;
        }
    }

    static double scoreConfidence(
            RawScraperProduct raw, List<DietaryTag> dietaryTags) {
        double score = 1.0;
        if (raw.priceFromText()) score -= 0.1;
        if (raw.imageUrl() == null || raw.imageUrl().isBlank()) score -= 0.2;
        if (dietaryTags.contains(DietaryTag.HALAL_LIKELY)
                || dietaryTags.contains(DietaryTag.ORGANIC_LIKELY)) {
            score -= 0.15;
        }
        String canonical = canonicalize(raw.displayName());
        if (displayNameDivergent(raw.displayName(), canonical)) score -= 0.1;
        if (raw.category() == ProductCategory.GROCERY && raw.unitPrice() == null) {
            score -= 0.1;
        }
        return Math.max(0.0, Math.min(1.0, score));
    }

    private static boolean displayNameDivergent(String displayName, String canonical) {
        if (displayName == null || canonical == null) return false;
        int displayLen = displayName.replaceAll("\\s+", " ").trim().length();
        int canonicalLen = canonical.length();
        if (displayLen == 0) return false;
        double ratio = (double) canonicalLen / (double) displayLen;
        return ratio < 0.5;
    }

    // exposed for tests
    static List<String> trackingParamPrefixes() {
        return new ArrayList<>(TRACKING_PARAM_PREFIXES);
    }

    static List<String> trackingParamExact() {
        return new ArrayList<>(TRACKING_PARAM_EXACT);
    }

    static List<String> canonicalizeTokens(String s) {
        return Arrays.asList(canonicalize(s).split(" "));
    }
}
