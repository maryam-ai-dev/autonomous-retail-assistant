package com.aisleon.basket.api;

import com.aisleon.basket.application.BasketApprovalService;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/baskets")
@Tag(name = "Baskets")
public class BasketController {

    private final BasketApprovalService approvalService;

    public BasketController(BasketApprovalService approvalService) {
        this.approvalService = approvalService;
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
}
