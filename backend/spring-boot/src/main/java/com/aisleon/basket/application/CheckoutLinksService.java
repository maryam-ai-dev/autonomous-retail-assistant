package com.aisleon.basket.application;

import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import com.aisleon.catalogue.Retailer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutLinksService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutLinksService.class);

    private final BasketRepository basketRepository;
    private final RetailerCheckoutUrlService urlService;

    public CheckoutLinksService(
            BasketRepository basketRepository, RetailerCheckoutUrlService urlService) {
        this.basketRepository = basketRepository;
        this.urlService = urlService;
    }

    @Transactional(readOnly = true)
    public Map<String, String> linksFor(UUID basketId) {
        BasketJpaEntity basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new BasketNotFoundException(basketId));
        Map<String, String> out = new LinkedHashMap<>();
        for (String retailerName : basket.getRetailersUsed()) {
            Retailer retailer;
            try {
                retailer = Retailer.valueOf(retailerName);
            } catch (IllegalArgumentException e) {
                log.info(
                        "CheckoutLinksGenerated: basketId={} skipped unknown retailer={}",
                        basketId,
                        retailerName);
                continue;
            }
            urlService.urlFor(retailer).ifPresent(url -> out.put(retailer.name(), url));
        }
        log.info(
                "CheckoutLinksGenerated: basketId={} retailers={}", basketId, out.keySet());
        return out;
    }
}
