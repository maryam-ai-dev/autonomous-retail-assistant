package com.aisleon.scraping.apify;

import com.aisleon.catalogue.OfferFlag;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TescoApifyConnector extends ApifyConnector {

    public static final String ACTOR_ID = "jupri/tesco-grocery";

    public TescoApifyConnector(ApifyClient apify) {
        super(apify, ACTOR_ID);
    }

    @Override
    public Retailer getRetailer() {
        return Retailer.TESCO;
    }

    @Override
    protected Map<String, Object> buildInput(String query, int maxResults) {
        return Map.of(
                "search", query,
                "maxItems", maxResults);
    }

    @Override
    protected List<RawScraperProduct> parse(List<Map<String, Object>> rawItems) {
        List<RawScraperProduct> out = new ArrayList<>(rawItems.size());
        Instant now = Instant.now();
        for (Map<String, Object> item : rawItems) {
            String id = string(item.get("id"));
            String name = string(item.get("name"));
            if (id == null || name == null) continue;
            BigDecimal price = decimal(item.get("price"));
            BigDecimal unitPrice = decimal(item.get("unitPrice"));
            String unitBasis = string(item.get("unitOfMeasure"));
            String brand = string(item.get("brand"));
            String imageUrl = string(item.get("imageUrl"));
            String productUrl = string(item.get("url"));
            String sizeText = string(item.get("size"));
            List<OfferFlag> offers = new ArrayList<>();
            if (Boolean.TRUE.equals(item.get("isClubcardPrice"))) {
                offers.add(OfferFlag.CLUBCARD_PRICE);
            }
            out.add(new RawScraperProduct(
                    id,
                    name,
                    brand,
                    ProductCategory.GROCERY,
                    ProductSubcategory.UNKNOWN,
                    price,
                    price == null,
                    unitPrice,
                    unitBasis,
                    sizeText,
                    imageUrl,
                    productUrl,
                    true,
                    true,
                    List.of(),
                    List.copyOf(offers),
                    now));
        }
        return out;
    }

    private static String string(Object value) {
        if (value == null) return null;
        String s = value.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
