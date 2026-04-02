package com.aisleon.discovery.infrastructure.connectors.api.base;

import com.aisleon.preferences.domain.RetailPreferences;

public interface ApiConnector {

    ApiConnectorResult search(String query, RetailPreferences preferences);
}
