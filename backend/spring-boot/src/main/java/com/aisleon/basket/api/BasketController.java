package com.aisleon.basket.api;

import com.aisleon.basket.application.BasketApprovalService;
import com.aisleon.basket.application.BasketFlagService;
import com.aisleon.basket.application.CheckoutLinksService;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/baskets")
@Tag(name = "Baskets")
public class BasketController {

    private final BasketApprovalService approvalService;
    private final BasketFlagService flagService;
    private final CheckoutLinksService checkoutLinksService;

    public BasketController(
            BasketApprovalService approvalService,
            BasketFlagService flagService,
            CheckoutLinksService checkoutLinksService) {
        this.approvalService = approvalService;
        this.flagService = flagService;
        this.checkoutLinksService = checkoutLinksService;
    }

    @Operation(
            summary = "Approve a DRAFT basket",
            description =
                    "Transitions a DRAFT basket to APPROVED. Returns 409 with"
                            + " { unresolvedCount: N } when substitution flags are"
                            + " unresolved; 422 with { reason: BUDGET_EXCEEDED } if the"
                            + " basket total exceeds the intent's budget; 404 if no basket"
                            + " with this id exists.")
    @PostMapping("/{id}/approve")
    public ResponseEntity<BasketDto> approve(@PathVariable("id") UUID id) {
        BasketJpaEntity approved = approvalService.approve(id);
        return ResponseEntity.ok(BasketDto.fromEntity(approved));
    }

    @Operation(
            summary = "Mark a substitution flag as accepted on a basket item",
            description =
                    "Returns 400 if the targeted item has no flag; 404 if the basket"
                            + " or item cannot be found.")
    @PostMapping("/{id}/items/{itemId}/resolve-flag")
    public ResponseEntity<BasketDto> resolveFlag(
            @PathVariable("id") UUID id, @PathVariable("itemId") UUID itemId) {
        BasketJpaEntity updated = flagService.resolveFlag(id, itemId);
        return ResponseEntity.ok(BasketDto.fromEntity(updated));
    }

    @Operation(
            summary = "Get handoff checkout URLs for a basket",
            description =
                    "Returns a map of retailer key to handoff URL. Retailers that do not"
                            + " have a configured URL are omitted — never 500.")
    @GetMapping("/{id}/checkout-links")
    public ResponseEntity<Map<String, String>> checkoutLinks(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(checkoutLinksService.linksFor(id));
    }
}
