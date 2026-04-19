package com.aisleon.scraping.apify;

import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import com.aisleon.scraping.CircuitState;
import com.aisleon.scraping.ConnectorCircuitBreaker;
import com.aisleon.scraping.ConnectorStatus;
import com.aisleon.scraping.RetailerConnector;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ApifyConnector implements RetailerConnector {

    private static final Logger log = LoggerFactory.getLogger(ApifyConnector.class);

    protected final ApifyClient apify;
    protected final String actorId;
    private final ConnectorCircuitBreaker breaker;

    private final AtomicReference<Instant> lastSuccessAt = new AtomicReference<>();
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>();
    private final AtomicReference<String> lastFailureReason = new AtomicReference<>();
    private final AtomicInteger recentResultCount = new AtomicInteger(0);

    protected ApifyConnector(ApifyClient apify, String actorId) {
        this(apify, actorId, new ConnectorCircuitBreaker());
    }

    protected ApifyConnector(
            ApifyClient apify, String actorId, ConnectorCircuitBreaker breaker) {
        this.apify = apify;
        this.actorId = actorId;
        this.breaker = breaker;
    }

    @Override
    public List<RawScraperProduct> search(String query, int maxResults) {
        if (!breaker.allowRequest()) {
            return List.of();
        }
        Map<String, Object> input = buildInput(query, maxResults);
        ApifyException lastError = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                List<Map<String, Object>> rawItems = apify.runSync(actorId, input);
                List<RawScraperProduct> products = parse(rawItems);
                lastSuccessAt.set(Instant.now());
                recentResultCount.set(products.size());
                breaker.recordSuccess();
                return products;
            } catch (ApifyException e) {
                lastError = e;
                if (!e.isTimeout()) break;
                log.warn("Apify timeout on attempt {} for {}", attempt + 1, getRetailer());
            }
        }
        lastFailureAt.set(Instant.now());
        lastFailureReason.set(lastError != null ? lastError.getMessage() : "unknown");
        breaker.recordFailure();
        return List.of();
    }

    @Override
    public boolean isHealthy() {
        return breaker.currentState() != CircuitState.OPEN;
    }

    @Override
    public ConnectorStatus getStatus() {
        return new ConnectorStatus(
                getRetailer(),
                isHealthy(),
                false,
                breaker.currentState(),
                lastSuccessAt.get(),
                lastFailureAt.get(),
                lastFailureReason.get(),
                recentResultCount.get(),
                0,
                true);
    }

    protected abstract Map<String, Object> buildInput(String query, int maxResults);

    protected abstract List<RawScraperProduct> parse(List<Map<String, Object>> rawItems);

    protected static Retailer retailerFor(Retailer r) {
        return r;
    }
}
