package com.aisleon.discovery.interfaces;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryRequest {

    @NotBlank(message = "Query is required")
    private String query;

    @Builder.Default
    private int maxResults = 10;
}
