package com.aisleon.catalogue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class DeduplicationService {

    private static final double NAME_SIMILARITY_THRESHOLD = 0.85;
    private static final double SIZE_TOLERANCE = 0.20;
    private static final Pattern SIZE_PATTERN =
            Pattern.compile(
                    "(\\d+(?:\\.\\d+)?)\\s*(g|kg|ml|l|cl|oz|lb|pk|pack|ct)",
                    Pattern.CASE_INSENSITIVE);

    public List<NormalizedProduct> annotateCrossRetailerMatches(
            List<NormalizedProduct> products) {
        if (products == null || products.isEmpty()) return List.of();
        int n = products.size();
        Map<Integer, List<String>> matches = new HashMap<>();
        for (int i = 0; i < n; i++) matches.put(i, new ArrayList<>());

        for (int i = 0; i < n; i++) {
            NormalizedProduct a = products.get(i);
            for (int j = i + 1; j < n; j++) {
                NormalizedProduct b = products.get(j);
                if (a.retailer() == b.retailer()) continue;
                if (!sameSubcategory(a, b)) continue;
                if (!similarName(a.canonicalName(), b.canonicalName())) continue;
                if (!similarSize(a.sizeText(), b.sizeText())) continue;
                matches.get(i).add(b.externalId());
                matches.get(j).add(a.externalId());
            }
        }

        List<NormalizedProduct> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            NormalizedProduct p = products.get(i);
            List<String> ids = matches.get(i);
            out.add(withCrossRetailerIds(p, ids));
        }
        return out;
    }

    static boolean similarName(String a, String b) {
        return nameSimilarity(a, b) >= NAME_SIMILARITY_THRESHOLD;
    }

    static double nameSimilarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        Set<String> ta = tokens(a);
        Set<String> tb = tokens(b);
        if (ta.isEmpty() || tb.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(ta);
        inter.retainAll(tb);
        // Dice coefficient: 2 * |A ∩ B| / (|A| + |B|)
        return (2.0 * inter.size()) / (ta.size() + tb.size());
    }

    static boolean similarSize(String a, String b) {
        Optional<ParsedSize> pa = parseSize(a);
        Optional<ParsedSize> pb = parseSize(b);
        if (pa.isEmpty() && pb.isEmpty()) return true;
        if (pa.isEmpty() || pb.isEmpty()) return false;
        ParsedSize sa = pa.get();
        ParsedSize sb = pb.get();
        if (!sa.unit().equalsIgnoreCase(sb.unit())) return false;
        double denominator = Math.max(sa.amount(), sb.amount());
        if (denominator == 0) return true;
        double diff = Math.abs(sa.amount() - sb.amount()) / denominator;
        return diff <= SIZE_TOLERANCE;
    }

    static Optional<ParsedSize> parseSize(String sizeText) {
        if (sizeText == null || sizeText.isBlank()) return Optional.empty();
        Matcher matcher = SIZE_PATTERN.matcher(sizeText);
        if (!matcher.find()) return Optional.empty();
        try {
            double amount = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            return Optional.of(new ParsedSize(amount, unit));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    record ParsedSize(double amount, String unit) {}

    private static boolean sameSubcategory(NormalizedProduct a, NormalizedProduct b) {
        return a.subcategory() != null && a.subcategory() == b.subcategory();
    }

    private static Set<String> tokens(String s) {
        if (s == null) return Set.of();
        return new HashSet<>(Arrays.asList(s.split("\\s+")));
    }

    private static NormalizedProduct withCrossRetailerIds(
            NormalizedProduct p, List<String> ids) {
        return new NormalizedProduct(
                p.externalId(),
                p.canonicalName(),
                p.displayName(),
                p.brand(),
                p.retailer(),
                p.category(),
                p.subcategory(),
                p.price(),
                p.unitPrice(),
                p.unitBasis(),
                p.sizeText(),
                p.imageUrl(),
                p.productUrl(),
                p.isAvailable(),
                p.isBasketable(),
                p.dietaryTags(),
                p.certificationTags(),
                p.offerFlags(),
                p.confidenceScore(),
                p.sourceFetchedAt(),
                p.normalizationWarnings(),
                List.copyOf(ids));
    }
}
