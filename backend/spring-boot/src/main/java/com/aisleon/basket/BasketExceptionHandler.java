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

    @ExceptionHandler(OutOfScopeException.class)
    public ResponseEntity<Map<String, String>> handleOutOfScope(OutOfScopeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of(
                        "reason", "OUT_OF_SCOPE",
                        "outOfScopeReason", ex.reason() == null ? "" : ex.reason(),
                        "message",
                        "Aisleon covers health, beauty, homeware, fashion, and electronics."
                                + " For grocery, try NourishOS."));
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

    @ExceptionHandler(UnresolvedFlagsException.class)
    public ResponseEntity<Map<String, Object>> handleUnresolved(UnresolvedFlagsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "reason", "UNRESOLVED_FLAGS",
                        "unresolvedCount", ex.unresolvedCount()));
    }

    @ExceptionHandler(BudgetExceededException.class)
    public ResponseEntity<Map<String, Object>> handleBudgetExceeded(BudgetExceededException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of(
                        "reason", "BUDGET_EXCEEDED",
                        "totalCost", ex.totalCost(),
                        "budget", ex.budget()));
    }

    @ExceptionHandler(BasketNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(BasketNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("reason", "BASKET_NOT_FOUND"));
    }

    @ExceptionHandler(BasketItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleItemNotFound(BasketItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("reason", "BASKET_ITEM_NOT_FOUND"));
    }

    @ExceptionHandler(FlagNotPresentException.class)
    public ResponseEntity<Map<String, String>> handleFlagNotPresent(FlagNotPresentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("reason", "FLAG_NOT_PRESENT"));
    }
}
