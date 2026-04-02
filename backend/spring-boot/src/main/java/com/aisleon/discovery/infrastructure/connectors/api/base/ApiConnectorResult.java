package com.aisleon.discovery.infrastructure.connectors.api.base;

import com.aisleon.discovery.domain.NormalizedProduct;

import java.util.List;

public record ApiConnectorResult(
        List<NormalizedProduct> products,
        String sourceName,
        boolean success,
        String errorMessage
) {
}
