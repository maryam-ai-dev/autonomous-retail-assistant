package com.aisleon.scraping.bridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import com.aisleon.scraping.CircuitState;
import com.aisleon.scraping.ConnectorUnavailableException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScraperBridgeConnectorTest {

    private final ScraperBridgeClient bridgeClient = mock(ScraperBridgeClient.class);

    @Test
    void successDelegatesAndUpdatesStatus() {
        ScraperBridgeConnector connector =
                new ScraperBridgeConnector(Retailer.ASOS, bridgeClient);
        RawScraperProduct raw = new RawScraperProduct(
                "a1",
                "Black midi dress",
                "ASOS DESIGN",
                ProductCategory.FASHION,
                ProductSubcategory.DRESSES,
                new BigDecimal("45.00"),
                false,
                null,
                null,
                "XS, S, M, L",
                null,
                "https://www.asos.com/p/a1",
                true,
                true,
                List.of(),
                List.of(),
                Instant.now());
        when(bridgeClient.search(any(), anyString(), anyInt())).thenReturn(List.of(raw));

        List<RawScraperProduct> out = connector.search("dress", 5);
        assertThat(out).hasSize(1);
        assertThat(connector.getStatus().recentResultCount()).isEqualTo(1);
        assertThat(connector.getStatus().lastSuccessAt()).isNotNull();
        assertThat(connector.getStatus().healthy()).isTrue();
    }

    @Test
    void bridgeUnavailableIsPropagatedAndStatusReflectsFailure() {
        ScraperBridgeConnector connector =
                new ScraperBridgeConnector(Retailer.BOOTS, bridgeClient);
        when(bridgeClient.search(any(), anyString(), anyInt()))
                .thenThrow(new ConnectorUnavailableException(Retailer.BOOTS, "bridge timeout"));

        assertThatThrownBy(() -> connector.search("toothpaste", 5))
                .isInstanceOf(ConnectorUnavailableException.class);
        assertThat(connector.getStatus().lastFailureReason()).contains("timeout");
    }

    @Test
    void circuitOpensAfterThreeConsecutiveFailures() {
        ScraperBridgeConnector connector =
                new ScraperBridgeConnector(Retailer.ARGOS, bridgeClient);
        when(bridgeClient.search(any(), anyString(), anyInt()))
                .thenThrow(new ConnectorUnavailableException(Retailer.ARGOS, "500"));

        for (int i = 0; i < 3; i++) {
            try {
                connector.search("kettle", 5);
            } catch (ConnectorUnavailableException ignored) {
                // expected
            }
        }
        assertThat(connector.getStatus().circuitState()).isEqualTo(CircuitState.OPEN);
        assertThat(connector.isHealthy()).isFalse();
        assertThatThrownBy(() -> connector.search("kettle", 5))
                .isInstanceOf(ConnectorUnavailableException.class)
                .hasMessageContaining("circuit open");
    }
}
