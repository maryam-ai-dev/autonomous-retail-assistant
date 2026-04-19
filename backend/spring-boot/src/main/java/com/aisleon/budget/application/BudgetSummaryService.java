package com.aisleon.budget.application;

import com.aisleon.audit.repository.AuditEventJpaEntity;
import com.aisleon.audit.repository.AuditRepository;
import com.aisleon.budget.api.BudgetSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetSummaryService {

    private static final Logger log = LoggerFactory.getLogger(BudgetSummaryService.class);
    private static final String EVENT_TYPE = "BASKET_APPROVED";

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public BudgetSummaryService(AuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public BudgetSummary summaryFor(UUID userId, YearMonth month) {
        if (month.isAfter(YearMonth.now())) {
            return zeroSummary(month);
        }
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

        List<AuditEventJpaEntity> events = auditRepository
                .findByUserIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        userId, EVENT_TYPE, start, end);
        if (events.isEmpty()) return zeroSummary(month);

        BigDecimal spent = BigDecimal.ZERO;
        BigDecimal budget = BigDecimal.ZERO;
        Map<String, BigDecimal> byRetailer = new LinkedHashMap<>();

        for (AuditEventJpaEntity event : events) {
            ApprovedPayload p = readPayload(event.getPayload());
            if (p == null) continue;
            spent = spent.add(p.totalCost == null ? BigDecimal.ZERO : p.totalCost);
            budget = budget.add(p.budget == null ? BigDecimal.ZERO : p.budget);
            attributeRetailerSpend(byRetailer, p);
        }

        return BudgetSummary.builder()
                .month(month)
                .spent(spent)
                .budget(budget)
                .savedVsFullPrice(BigDecimal.ZERO)
                .byRetailer(byRetailer)
                .basketCount(events.size())
                .build();
    }

    private void attributeRetailerSpend(Map<String, BigDecimal> byRetailer, ApprovedPayload p) {
        if (p.retailersUsed == null || p.retailersUsed.isEmpty()) return;
        if (p.totalCost == null) return;
        BigDecimal share = p.totalCost.divide(
                BigDecimal.valueOf(p.retailersUsed.size()), 2, RoundingMode.HALF_UP);
        for (String r : p.retailersUsed) {
            byRetailer.merge(r, share, BigDecimal::add);
        }
    }

    private ApprovedPayload readPayload(Map<String, Object> raw) {
        if (raw == null) return null;
        try {
            return objectMapper.convertValue(raw, ApprovedPayload.class);
        } catch (IllegalArgumentException ex) {
            log.warn("BASKET_APPROVED payload unreadable: {}", ex.getMessage());
            return null;
        }
    }

    private static BudgetSummary zeroSummary(YearMonth month) {
        return BudgetSummary.builder()
                .month(month)
                .spent(BigDecimal.ZERO)
                .budget(BigDecimal.ZERO)
                .savedVsFullPrice(BigDecimal.ZERO)
                .byRetailer(Collections.emptyMap())
                .basketCount(0)
                .build();
    }

    private static class ApprovedPayload {
        public BigDecimal totalCost;
        public BigDecimal budget;
        public List<String> retailersUsed;
    }
}
