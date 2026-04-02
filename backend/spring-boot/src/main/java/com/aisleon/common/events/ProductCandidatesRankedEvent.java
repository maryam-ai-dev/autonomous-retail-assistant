package com.aisleon.common.events;

import java.util.List;
import java.util.UUID;

public record ProductCandidatesRankedEvent(
        UUID userId,
        String query,
        List<String> topProductTitles,
        double confidence,
        String strategyUsed
) {
}
