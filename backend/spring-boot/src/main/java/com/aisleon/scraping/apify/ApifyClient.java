package com.aisleon.scraping.apify;

import java.util.List;
import java.util.Map;

public interface ApifyClient {

    /**
     * Runs an Apify actor synchronously and returns the dataset items.
     *
     * @throws ApifyException if the run fails (HTTP error, timeout, invalid key)
     */
    List<Map<String, Object>> runSync(String actorId, Map<String, Object> input)
            throws ApifyException;
}
