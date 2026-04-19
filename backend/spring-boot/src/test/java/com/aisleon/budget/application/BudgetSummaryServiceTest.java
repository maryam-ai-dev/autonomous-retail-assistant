package com.aisleon.budget.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aisleon.audit.repository.AuditEventJpaEntity;
import com.aisleon.audit.repository.AuditRepository;
import com.aisleon.budget.api.BudgetSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BudgetSummaryServiceTest {

    private final AuditRepository auditRepository = mock(AuditRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BudgetSummaryService service =
            new BudgetSummaryService(auditRepository, objectMapper);

    @Test
    void futureMonthReturnsAllZerosWithoutQueryingRepo() {
        UUID userId = UUID.randomUUID();
        YearMonth future = YearMonth.now().plusMonths(2);

        BudgetSummary out = service.summaryFor(userId, future);
        assertThat(out.getSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(out.getBudget()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(out.getSavedVsFullPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(out.getByRetailer()).isEmpty();
        assertThat(out.getBasketCount()).isZero();
        assertThat(out.getMonth()).isEqualTo(future);
        verify(auditRepository, never())
                .findByUserIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        any(), any(), any(), any());
    }

    @Test
    void noBasketsInMonthReturnsZeros() {
        UUID userId = UUID.randomUUID();
        YearMonth month = YearMonth.of(2026, 4);
        when(auditRepository
                        .findByUserIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                eq(userId), eq("BASKET_APPROVED"), any(), any()))
                .thenReturn(List.of());

        BudgetSummary out = service.summaryFor(userId, month);
        assertThat(out.getSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(out.getBudget()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(out.getByRetailer()).isEmpty();
        assertThat(out.getBasketCount()).isZero();
    }

    @Test
    void aggregatesTotalsAcrossMultipleApprovedBaskets() {
        UUID userId = UUID.randomUUID();
        YearMonth month = YearMonth.of(2026, 4);

        AuditEventJpaEntity e1 = audit(userId, payload("65.20", "70.00", List.of("TESCO")));
        AuditEventJpaEntity e2 = audit(userId, payload("48.00", "50.00", List.of("BOOTS", "ARGOS")));
        AuditEventJpaEntity e3 = audit(userId, payload("12.00", null, List.of("SAINSBURYS")));

        when(auditRepository
                        .findByUserIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                eq(userId), eq("BASKET_APPROVED"), any(), any()))
                .thenReturn(List.of(e1, e2, e3));

        BudgetSummary out = service.summaryFor(userId, month);
        assertThat(out.getBasketCount()).isEqualTo(3);
        assertThat(out.getSpent()).isEqualByComparingTo(new BigDecimal("125.20"));
        assertThat(out.getBudget()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(out.getSavedVsFullPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(out.getByRetailer())
                .containsEntry("TESCO", new BigDecimal("65.20"))
                .containsEntry("BOOTS", new BigDecimal("24.00"))
                .containsEntry("ARGOS", new BigDecimal("24.00"))
                .containsEntry("SAINSBURYS", new BigDecimal("12.00"));
    }

    @Test
    void unreadablePayloadIsSkipped() {
        UUID userId = UUID.randomUUID();
        YearMonth month = YearMonth.of(2026, 4);

        Map<String, Object> garbage = new HashMap<>();
        garbage.put("totalCost", "not-a-number");
        AuditEventJpaEntity bad = audit(userId, garbage);
        AuditEventJpaEntity good = audit(userId, payload("10.00", "15.00", List.of("TESCO")));

        when(auditRepository
                        .findByUserIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                eq(userId), eq("BASKET_APPROVED"), any(), any()))
                .thenReturn(List.of(bad, good));

        BudgetSummary out = service.summaryFor(userId, month);
        assertThat(out.getBasketCount()).isEqualTo(2); // count includes raw event count
        assertThat(out.getSpent()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    private static Map<String, Object> payload(
            String totalCost, String budget, List<String> retailers) {
        Map<String, Object> p = new HashMap<>();
        p.put("totalCost", totalCost);
        if (budget != null) p.put("budget", budget);
        p.put("retailersUsed", retailers);
        return p;
    }

    private static AuditEventJpaEntity audit(UUID userId, Map<String, Object> payload) {
        return AuditEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventType("BASKET_APPROVED")
                .entityType("basket")
                .entityId(UUID.randomUUID().toString())
                .payload(payload)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
