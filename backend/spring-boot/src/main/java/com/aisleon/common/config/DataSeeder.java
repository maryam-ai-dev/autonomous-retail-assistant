package com.aisleon.common.config;

import com.aisleon.merchant.infrastructure.MerchantJpaEntity;
import com.aisleon.merchant.infrastructure.MerchantRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements ApplicationRunner {

    private final MerchantRepository merchantRepository;

    public DataSeeder(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (merchantRepository.count() > 0) {
            return;
        }

        merchantRepository.save(MerchantJpaEntity.builder()
                .name("eBay")
                .sourceType("API")
                .isApproved(true)
                .trustScore(new BigDecimal("0.90"))
                .build());

        merchantRepository.save(MerchantJpaEntity.builder()
                .name("GenericBrowser")
                .sourceType("BROWSER")
                .isApproved(false)
                .trustScore(new BigDecimal("0.50"))
                .build());
    }
}
