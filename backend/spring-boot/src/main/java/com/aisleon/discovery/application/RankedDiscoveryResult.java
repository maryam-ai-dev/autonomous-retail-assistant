package com.aisleon.discovery.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Internal Spring-side DTO mirroring the Python RankingResponse shape.
 * Not exposed externally — used to populate DiscoveryResult.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedDiscoveryResult {

    private List<RankedProductDto> rankedProducts;
    private String strategyUsed;
    private double confidence;
    private UncertaintyDto uncertainty;
    private int filteredCount;
    private List<String> sourcesUsed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankedProductDto {
        private Map<String, Object> product;
        private double score;
        private int rank;
        private Map<String, Object> explanation;
        private Map<String, Object> trustScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UncertaintyDto {
        private double confidence;
        private boolean isUncertain;
        private List<String> reasons;
        private String recommendation;
    }
}
