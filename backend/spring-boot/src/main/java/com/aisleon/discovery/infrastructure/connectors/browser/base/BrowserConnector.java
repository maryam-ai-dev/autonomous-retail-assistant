package com.aisleon.discovery.infrastructure.connectors.browser.base;

import com.aisleon.preferences.domain.RetailPreferences;

public interface BrowserConnector {

    BrowserConnectorResult search(String query, RetailPreferences preferences);
}
