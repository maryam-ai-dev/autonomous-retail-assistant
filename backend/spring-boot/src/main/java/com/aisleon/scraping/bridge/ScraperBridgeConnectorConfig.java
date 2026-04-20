package com.aisleon.scraping.bridge;

import com.aisleon.catalogue.Retailer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the v1.1 Playwright-bridged retailers as beans so the
 * {@link com.aisleon.scraping.ConnectorRegistry} picks them up alongside any
 * directly-implemented connectors.
 *
 * <p>Added in sprint B12.2 — v1.1 retails through Boots, Argos, and ASOS via
 * the FastAPI Playwright bridge. Grocery retailers were archived in B12.1.
 */
@Configuration
public class ScraperBridgeConnectorConfig {

    @Bean
    public ScraperBridgeConnector bootsBridgeConnector(ScraperBridgeClient client) {
        return new ScraperBridgeConnector(Retailer.BOOTS, client);
    }

    @Bean
    public ScraperBridgeConnector argosBridgeConnector(ScraperBridgeClient client) {
        return new ScraperBridgeConnector(Retailer.ARGOS, client);
    }

    @Bean
    public ScraperBridgeConnector asosBridgeConnector(ScraperBridgeClient client) {
        return new ScraperBridgeConnector(Retailer.ASOS, client);
    }
}
