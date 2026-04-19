package com.aisleon.approval.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Deprecated approval-queue endpoints from the trust-aware retail prototype.
 * Basket approval now happens inline via {@code POST /api/baskets/{id}/approve}
 * (sprint B7.1).
 *
 * <p>All routes here return 410 Gone per sprint B11.2. Internal approval events
 * are still emitted by upstream cart/checkout flows and consumed by the audit
 * log — only the user-facing queue API is gone.
 */
@RestController
@RequestMapping("/api/approvals")
@Tag(name = "Deprecated — approvals queue (use /api/baskets/{id}/approve)")
public class ApprovalController {

    private static final String REPLACEMENT = "POST /api/baskets/{id}/approve";

    @Operation(summary = "GONE — basket approval is now inline.")
    @GetMapping
    public ResponseEntity<Map<String, String>> listGone() {
        return gone();
    }

    @Operation(summary = "GONE — basket approval is now inline.")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getGone(@PathVariable("id") UUID id) {
        return gone();
    }

    @Operation(summary = "GONE — basket approval is now inline.")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveGone(@PathVariable("id") UUID id) {
        return gone();
    }

    @Operation(summary = "GONE — basket approval is now inline.")
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectGone(@PathVariable("id") UUID id) {
        return gone();
    }

    private static ResponseEntity<Map<String, String>> gone() {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("reason", "ENDPOINT_GONE", "replacement", REPLACEMENT));
    }
}
