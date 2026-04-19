package com.aisleon.basket.application;

import com.aisleon.catalogue.Retailer;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Maps a {@link Retailer} to its handoff checkout URL.
 *
 * <p>v1 has no retailer-API integrations, so "deep link" means the user is
 * sent to the retailer's own site to complete checkout. Patterns are
 * configurable via {@code checkout.url.*} properties so we can swap in real
 * cart-prefill links later without re-deploying clients.
 */
@Service
public class RetailerCheckoutUrlService {

    private final Map<Retailer, String> urls;

    public RetailerCheckoutUrlService(
            @Value("${checkout.url.tesco:https://www.tesco.com/groceries/}") String tesco,
            @Value("${checkout.url.sainsburys:https://www.sainsburys.co.uk/shop/gb/groceries}")
                    String sainsburys,
            @Value("${checkout.url.boots:https://www.boots.com}") String boots,
            @Value("${checkout.url.argos:https://www.argos.co.uk}") String argos) {
        this.urls = Map.of(
                Retailer.TESCO, tesco,
                Retailer.SAINSBURYS, sainsburys,
                Retailer.BOOTS, boots,
                Retailer.ARGOS, argos);
    }

    public Optional<String> urlFor(Retailer retailer) {
        return Optional.ofNullable(urls.get(retailer));
    }
}
