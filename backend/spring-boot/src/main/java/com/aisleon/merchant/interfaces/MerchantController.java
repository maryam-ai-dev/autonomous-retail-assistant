package com.aisleon.merchant.interfaces;

import com.aisleon.merchant.application.MerchantService;
import com.aisleon.merchant.domain.Merchant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    public ResponseEntity<List<Merchant>> getMerchants() {
        return ResponseEntity.ok(merchantService.getApprovedMerchants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Merchant> getMerchant(@PathVariable UUID id) {
        return ResponseEntity.ok(merchantService.getMerchantById(id));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Merchant> approveMerchant(@PathVariable UUID id) {
        return ResponseEntity.ok(merchantService.approveMerchant(id));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<Merchant> blockMerchant(@PathVariable UUID id) {
        return ResponseEntity.ok(merchantService.blockMerchant(id));
    }
}
