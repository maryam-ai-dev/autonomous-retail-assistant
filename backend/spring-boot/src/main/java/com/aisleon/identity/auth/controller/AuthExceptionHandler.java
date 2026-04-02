package com.aisleon.identity.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage();
        HttpStatus status = message.contains("Invalid email or password")
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(Map.of("error", message));
    }
}
