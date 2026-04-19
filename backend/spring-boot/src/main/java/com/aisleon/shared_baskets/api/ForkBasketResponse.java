package com.aisleon.shared_baskets.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of forking a shared basket — the new basket id for the calling user.")
public class ForkBasketResponse {

    private UUID basketId;
}
