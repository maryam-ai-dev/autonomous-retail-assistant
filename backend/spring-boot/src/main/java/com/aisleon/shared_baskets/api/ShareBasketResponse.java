package com.aisleon.shared_baskets.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of sharing an APPROVED basket — gives the public share id and URL.")
public class ShareBasketResponse {

    private String shareId;
    private String shareUrl;
}
