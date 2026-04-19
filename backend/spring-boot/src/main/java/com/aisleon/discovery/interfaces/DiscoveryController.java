package com.aisleon.discovery.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Deprecated discovery endpoint from the trust-aware retail prototype. The
 * current Aisleon flow uses {@code POST /api/basket-intent/submit} instead.
 *
 * <p>All routes here return 410 Gone per sprint B11.2.
 */
@RestController
@RequestMapping("/api/discovery")
@Tag(name = "Deprecated — discovery (use /api/basket-intent)")
public class DiscoveryController {

    @Operation(
            summary = "GONE — moved to POST /api/basket-intent/submit",
            description =
                    "Returns 410 Gone. The previous discovery flow has been replaced by the"
                            + " basket-intent submission endpoint, which produces a DRAFT"
                            + " basket from a free-text intent.")
    @PostMapping("/search")
    public ResponseEntity<Map<String, String>> searchGone() {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of(
                        "reason", "ENDPOINT_GONE",
                        "replacement", "POST /api/basket-intent/submit"));
    }
}
