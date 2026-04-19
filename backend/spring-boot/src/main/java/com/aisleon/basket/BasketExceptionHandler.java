package com.aisleon.basket;

import com.aisleon.basket.bridge.AiBridgeException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BasketExceptionHandler {

    @ExceptionHandler(ClothingProfileIncompleteException.class)
    public ResponseEntity<Map<String, String>> handleClothingIncomplete(
            ClothingProfileIncompleteException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                .body(Map.of("reason", "CLOTHING_PROFILE_INCOMPLETE"));
    }

    @ExceptionHandler(BudgetTooLowForIntentException.class)
    public ResponseEntity<Map<String, String>> handleBudgetTooLow(
            BudgetTooLowForIntentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of(
                        "reason", "BUDGET_TOO_LOW_FOR_INTENT",
                        "detail", ex.getMessage()));
    }

    @ExceptionHandler(NoRetailersAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleNoRetailers(
            NoRetailersAvailableException ex) {
        Map<String, String> reasons = ex.failureReasons().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> e.getValue() == null ? "unknown" : e.getValue(),
                        (a, b) -> a,
                        LinkedHashMap::new));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "reason", "NO_RETAILERS_AVAILABLE",
                        "failureReasons", reasons));
    }

    @ExceptionHandler(AiBridgeException.class)
    public ResponseEntity<Map<String, String>> handleAiBridge(AiBridgeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "reason", "AI_SERVICE_UNAVAILABLE",
                        "detail", ex.getMessage() == null ? "" : ex.getMessage()));
    }
}
