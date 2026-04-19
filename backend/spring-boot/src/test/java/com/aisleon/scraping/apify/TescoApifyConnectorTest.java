package com.aisleon.scraping.apify;

import static org.assertj.core.api.Assertions.assertThat;

import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.scraping.CircuitState;
import com.aisleon.scraping.ConnectorStatus;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TescoApifyConnectorTest {

    @Test
    void parsesApifyItemsIntoRawProducts() {
        StubClient client = new StubClient(List.of(
                Map.of("id", "t1", "name", "Milk 2pt", "brand", "Tesco",
                        "price", "1.30", "unitPrice", "0.65", "url", "https://tesco.com/1",
                        "imageUrl", "https://img/1.jpg", "size", "1.136L",
                        "isClubcardPrice", true),
                Map.of("id", "t2", "name", "Bread", "price", "1.05",
                        "url", "https://tesco.com/2")));
        TescoApifyConnector connector = new TescoApifyConnector(client);

        List<RawScraperProduct> products = connector.search("milk", 10);

        assertThat(products).hasSize(2);
        assertThat(products.get(0).externalId()).isEqualTo("t1");
        assertThat(products.get(0).offerFlags())
                .extracting(Enum::name)
                .contains("CLUBCARD_PRICE");
        ConnectorStatus status = connector.getStatus();
        assertThat(status.apifyConnector()).isTrue();
        assertThat(status.circuitState()).isEqualTo(CircuitState.CLOSED);
        assertThat(status.recentResultCount()).isEqualTo(2);
    }

    @Test
    void timeoutTriggersSingleRetryAndRecordsFailure() {
        StubClient client = StubClient.alwaysTimeout();
        TescoApifyConnector connector = new TescoApifyConnector(client);

        List<RawScraperProduct> products = connector.search("milk", 10);

        assertThat(products).isEmpty();
        assertThat(client.callCount).isEqualTo(2);
        // single failure — circuit still CLOSED (threshold is 3)
        assertThat(connector.getStatus().circuitState())
                .isEqualTo(CircuitState.CLOSED);
        assertThat(connector.getStatus().lastFailureReason()).contains("timed out");
    }

    @Test
    void threeConsecutiveFailuresOpenCircuit() {
        StubClient client = StubClient.alwaysHttpError(500);
        TescoApifyConnector connector = new TescoApifyConnector(client);

        connector.search("milk", 10);
        connector.search("milk", 10);
        connector.search("milk", 10);

        assertThat(connector.getStatus().circuitState())
                .isEqualTo(CircuitState.OPEN);
        // Each 500 does not retry
        assertThat(client.callCount).isEqualTo(3);
    }

    @Test
    void nonTimeoutFailureDoesNotRetry() {
        StubClient client = StubClient.alwaysHttpError(401);
        TescoApifyConnector connector = new TescoApifyConnector(client);

        List<RawScraperProduct> products = connector.search("milk", 10);

        assertThat(products).isEmpty();
        assertThat(client.callCount).isEqualTo(1);
        // One failure under the threshold — circuit stays closed
        assertThat(connector.getStatus().circuitState())
                .isEqualTo(CircuitState.CLOSED);
    }

    private static class StubClient implements ApifyClient {
        private final List<Map<String, Object>> items;
        private final boolean alwaysTimeout;
        private final int httpError;
        int callCount = 0;

        StubClient(List<Map<String, Object>> items) {
            this.items = items;
            this.alwaysTimeout = false;
            this.httpError = 0;
        }

        private StubClient(boolean alwaysTimeout, int httpError) {
            this.items = List.of();
            this.alwaysTimeout = alwaysTimeout;
            this.httpError = httpError;
        }

        static StubClient alwaysTimeout() {
            return new StubClient(true, 0);
        }

        static StubClient alwaysHttpError(int status) {
            return new StubClient(false, status);
        }

        @Override
        public List<Map<String, Object>> runSync(
                String actorId, Map<String, Object> input) {
            callCount++;
            if (alwaysTimeout) throw new ApifyException("Apify timed out", true, 0);
            if (httpError > 0)
                throw new ApifyException("Apify HTTP " + httpError, false, httpError);
            return items;
        }
    }
}
