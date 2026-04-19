package com.aisleon.scraping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConnectorRegistryTest {

    @Test
    void getConnectorThrowsWhenRetailerDisabled() {
        StubConnector tesco = new StubConnector(Retailer.TESCO, true);
        StubConnector boots = new StubConnector(Retailer.BOOTS, true);
        ConnectorRegistry registry = new ConnectorRegistry(
                List.of(tesco, boots), "TESCO", new StaleCacheCounter());

        assertThatThrownBy(() -> registry.getConnector(Retailer.TESCO))
                .isInstanceOf(ConnectorUnavailableException.class);
        assertThat(registry.availableRetailers()).containsExactly(Retailer.BOOTS);
        assertThat(registry.isDisabled(Retailer.TESCO)).isTrue();
    }

    @Test
    void availableRetailersExcludesUnhealthyConnectors() {
        StubConnector tesco = new StubConnector(Retailer.TESCO, true);
        StubConnector boots = new StubConnector(Retailer.BOOTS, false);
        ConnectorRegistry registry = new ConnectorRegistry(
                List.of(tesco, boots), "", new StaleCacheCounter());

        assertThat(registry.availableRetailers()).containsExactly(Retailer.TESCO);
    }

    @Test
    void staleCacheUsageOverlaidOntoConnectorStatus() {
        StubConnector tesco = new StubConnector(Retailer.TESCO, true);
        StaleCacheCounter counter = new StaleCacheCounter();
        ConnectorRegistry registry =
                new ConnectorRegistry(List.of(tesco), "", counter);

        counter.record(Retailer.TESCO);
        counter.record(Retailer.TESCO);

        ConnectorStatus status =
                registry.allStatuses().stream()
                        .filter(s -> s.retailer() == Retailer.TESCO)
                        .findFirst()
                        .orElseThrow();
        assertThat(status.staleCacheUsageCount()).isEqualTo(2);
    }

    @Test
    void statusListContainsEveryRegisteredConnector() {
        StubConnector tesco = new StubConnector(Retailer.TESCO, true);
        StubConnector sainsburys = new StubConnector(Retailer.SAINSBURYS, true);
        StubConnector boots = new StubConnector(Retailer.BOOTS, true);
        StubConnector argos = new StubConnector(Retailer.ARGOS, false);
        ConnectorRegistry registry =
                new ConnectorRegistry(
                        List.of(tesco, sainsburys, boots, argos), "", new StaleCacheCounter());

        List<ConnectorStatus> statuses = registry.allStatuses();

        assertThat(statuses).hasSize(4);
        assertThat(statuses)
                .extracting(ConnectorStatus::retailer)
                .containsExactlyInAnyOrder(
                        Retailer.TESCO,
                        Retailer.SAINSBURYS,
                        Retailer.BOOTS,
                        Retailer.ARGOS);
    }

    private static class StubConnector implements RetailerConnector {
        private final Retailer retailer;
        private final boolean healthy;

        StubConnector(Retailer retailer, boolean healthy) {
            this.retailer = retailer;
            this.healthy = healthy;
        }

        @Override
        public Retailer getRetailer() {
            return retailer;
        }

        @Override
        public List<RawScraperProduct> search(String query, int maxResults) {
            return List.of();
        }

        @Override
        public boolean isHealthy() {
            return healthy;
        }

        @Override
        public ConnectorStatus getStatus() {
            return new ConnectorStatus(
                    retailer,
                    healthy,
                    false,
                    healthy ? CircuitState.CLOSED : CircuitState.OPEN,
                    healthy ? Instant.now() : null,
                    healthy ? null : Instant.now(),
                    healthy ? null : "stubbed failure",
                    0,
                    0,
                    false);
        }
    }
}
