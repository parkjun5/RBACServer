package com.roles.rbacserver.common.application.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public record ApiCommonResponse(
        int statusCode,
        String message,
        LocalDateTime timestamp
) {
    public static ResponseEntity<ApiCommonResponse> of(HttpStatus status, String message) {
        ApiCommonResponse errorResponse = new ApiCommonResponse(status.value(), message, LocalDateTime.now());
        return ResponseEntity.status(status).body(errorResponse);
    }
}
