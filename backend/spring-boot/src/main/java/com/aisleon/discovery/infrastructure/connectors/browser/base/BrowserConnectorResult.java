package com.aisleon.discovery.infrastructure.connectors.browser.base;

import com.aisleon.discovery.domain.NormalizedProduct;

import java.util.List;

public record BrowserConnectorResult(
        List<NormalizedProduct> products,
        String sourceName,
        boolean success,
        String errorMessage
) {
}
