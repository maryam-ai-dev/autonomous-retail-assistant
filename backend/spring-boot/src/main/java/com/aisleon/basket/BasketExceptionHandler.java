package com.aisleon.basket;

import java.util.Map;
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
}
