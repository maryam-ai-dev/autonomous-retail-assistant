package com.aisleon.approval.interfaces;

import com.aisleon.approval.application.ApprovalService;
import com.aisleon.approval.domain.ApprovalRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public ResponseEntity<List<ApprovalRequest>> getPendingApprovals(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(approvalService.getPendingApprovals(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalRequest> getApproval(@PathVariable UUID id) {
        return ResponseEntity.ok(approvalService.getApprovalById(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApprovalRequest> approve(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(approvalService.approve(id, userId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApprovalRequest> reject(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(approvalService.reject(id, userId));
    }
}
