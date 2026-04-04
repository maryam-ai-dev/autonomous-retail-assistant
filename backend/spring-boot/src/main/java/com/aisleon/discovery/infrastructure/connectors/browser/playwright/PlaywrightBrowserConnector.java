package com.aisleon.discovery.infrastructure.connectors.browser.playwright;

import com.aisleon.discovery.domain.NormalizedProduct;
import com.aisleon.discovery.infrastructure.connectors.browser.base.BrowserConnector;
import com.aisleon.discovery.infrastructure.connectors.browser.base.BrowserConnectorResult;
import com.aisleon.merchant.domain.SourceType;
import com.aisleon.preferences.domain.RetailPreferences;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic browser connector using Playwright to scrape Google Shopping results.
 * Real merchant-specific connectors should extend this with
 * site-specific selectors in connectors/browser/merchants/.
 */
@Component
public class PlaywrightBrowserConnector implements BrowserConnector {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightBrowserConnector.class);
    private static final int MAX_PRODUCTS = 5;
    private static final String SOURCE_NAME = "browser_generic";

    private final Browser browser;

    public PlaywrightBrowserConnector(Browser browser) {
        this.browser = browser;
    }

    @Override
    public BrowserConnectorResult search(String query, RetailPreferences preferences) {
        try {
            List<NormalizedProduct> products = scrapeProducts(query);
            log.info("Browser connector found {} products for query: {}", products.size(), query);
            return new BrowserConnectorResult(products, SOURCE_NAME, true, null);
        } catch (Exception e) {
            log.warn("Browser connector failed for query '{}': {}", query, e.getMessage());
            return new BrowserConnectorResult(List.of(), SOURCE_NAME, false, e.getMessage());
        }
    }

    private List<NormalizedProduct> scrapeProducts(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.google.com/shopping?q=" + encodedQuery;

        List<NormalizedProduct> products = new ArrayList<>();
        Page page = browser.newPage();

        try {
            page.navigate(url);
            page.waitForLoadState();

            // Google Shopping product cards
            Locator cards = page.locator(".sh-dgr__gr-auto");
            int count = Math.min(cards.count(), MAX_PRODUCTS);

            for (int i = 0; i < count; i++) {
                try {
                    Locator card = cards.nth(i);

                    String title = extractText(card, "h3");
                    String priceText = extractText(card, ".a8Pemb");
                    BigDecimal price = parsePrice(priceText);

                    if (title.isEmpty() || price.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    products.add(new NormalizedProduct(
                            SourceType.BROWSER,
                            SOURCE_NAME,
                            "browser-" + i + "-" + System.currentTimeMillis(),
                            title,
                            "",
                            "",
                            "",
                            price,
                            "USD",
                            "UNKNOWN",
                            null,
                            SOURCE_NAME,
                            null,
                            BigDecimal.ZERO,
                            "",
                            List.of(),
                            "",
                            Map.of(),
                            Instant.now()
                    ));
                } catch (Exception e) {
                    log.debug("Failed to parse product card {}: {}", i, e.getMessage());
                }
            }
        } finally {
            page.close();
        }

        return products;
    }

    private String extractText(Locator parent, String selector) {
        try {
            Locator element = parent.locator(selector).first();
            if (element.count() > 0) {
                return element.textContent().trim();
            }
        } catch (Exception e) {
            // Element not found
        }
        return "";
    }

    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return BigDecimal.ZERO;
        }
        String cleaned = priceText.replaceAll("[^0-9.]", "");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
