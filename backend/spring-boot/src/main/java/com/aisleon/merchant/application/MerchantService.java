package com.aisleon.merchant.application;

import com.aisleon.merchant.domain.Merchant;
import com.aisleon.merchant.infrastructure.MerchantJpaEntity;
import com.aisleon.merchant.infrastructure.MerchantMapper;
import com.aisleon.merchant.infrastructure.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MerchantService {

    private final MerchantRepository repository;

    public MerchantService(MerchantRepository repository) {
        this.repository = repository;
    }

    public List<Merchant> getApprovedMerchants() {
        return repository.findAllByIsApproved(true).stream()
                .map(MerchantMapper::toDomain)
                .toList();
    }

    public Merchant getMerchantById(UUID id) {
        MerchantJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with id: " + id));
        return MerchantMapper.toDomain(entity);
    }

    @Transactional
    public Merchant approveMerchant(UUID id) {
        MerchantJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with id: " + id));
        entity.setIsApproved(true);
        repository.save(entity);
        return MerchantMapper.toDomain(entity);
    }

    @Transactional
    public Merchant blockMerchant(UUID id) {
        MerchantJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with id: " + id));
        entity.setIsApproved(false);
        repository.save(entity);
        return MerchantMapper.toDomain(entity);
    }
}
