package com.aisleon.scraping.bridge;

import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import com.aisleon.scraping.CircuitState;
import com.aisleon.scraping.ConnectorCircuitBreaker;
import com.aisleon.scraping.ConnectorStatus;
import com.aisleon.scraping.ConnectorUnavailableException;
import com.aisleon.scraping.RetailerConnector;
import java.time.Instant;
import java.util.List;

/**
 * Thin Spring-side adapter that delegates retailer scraping to the FastAPI
 * intelligence service via {@link ScraperBridgeClient}. One instance is
 * registered per retailer — Boots, Argos, ASOS — so the ConnectorRegistry
 * treats bridged retailers the same as any other {@link RetailerConnector}.
 */
public class ScraperBridgeConnector implements RetailerConnector {

    private final Retailer retailer;
    private final ScraperBridgeClient bridgeClient;
    private final ConnectorCircuitBreaker circuitBreaker;

    private volatile Instant lastSuccessAt;
    private volatile Instant lastFailureAt;
    private volatile String lastFailureReason;
    private volatile int recentResultCount;

    public ScraperBridgeConnector(Retailer retailer, ScraperBridgeClient bridgeClient) {
        this(retailer, bridgeClient, new ConnectorCircuitBreaker());
    }

    public ScraperBridgeConnector(
            Retailer retailer,
            ScraperBridgeClient bridgeClient,
            ConnectorCircuitBreaker circuitBreaker) {
        this.retailer = retailer;
        this.bridgeClient = bridgeClient;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Retailer getRetailer() {
        return retailer;
    }

    @Override
    public List<RawScraperProduct> search(String query, int maxResults) {
        if (!circuitBreaker.allowRequest()) {
            throw new ConnectorUnavailableException(retailer, "circuit open");
        }
        try {
            List<RawScraperProduct> result = bridgeClient.search(retailer, query, maxResults);
            circuitBreaker.recordSuccess();
            lastSuccessAt = Instant.now();
            recentResultCount = result.size();
            return result;
        } catch (ConnectorUnavailableException ex) {
            circuitBreaker.recordFailure();
            lastFailureAt = Instant.now();
            lastFailureReason = ex.getMessage();
            throw ex;
        } catch (RuntimeException ex) {
            circuitBreaker.recordFailure();
            lastFailureAt = Instant.now();
            lastFailureReason = ex.getMessage();
            throw new ConnectorUnavailableException(retailer, ex.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        return circuitBreaker.currentState() != CircuitState.OPEN;
    }

    @Override
    public ConnectorStatus getStatus() {
        return new ConnectorStatus(
                retailer,
                isHealthy(),
                false,
                circuitBreaker.currentState(),
                lastSuccessAt,
                lastFailureAt,
                lastFailureReason,
                recentResultCount,
                0,
                false);
    }
}
