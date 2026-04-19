package com.aisleon.budget.api;

import com.aisleon.budget.application.BudgetSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/budget")
@Tag(name = "Budget")
public class BudgetController {

    private final BudgetSummaryService summaryService;

    public BudgetController(BudgetSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @Operation(
            summary = "Get the spend summary for one calendar month",
            description =
                    "Aggregates BASKET_APPROVED audit events for the calling user. Future"
                            + " months always return zeros (HTTP 200, never 404). Month must"
                            + " be in YYYY-MM format.")
    @GetMapping("/summary")
    public ResponseEntity<BudgetSummary> summary(
            Authentication auth, @RequestParam("month") String month) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        YearMonth ym = parseMonth(month);
        return ResponseEntity.ok(summaryService.summaryFor(userId, ym));
    }

    private static YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "month must be in YYYY-MM format");
        }
    }
}
