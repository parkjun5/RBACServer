package com.roles.rbacserver.common.exception;

import com.roles.rbacserver.login.exception.IllegalTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice(basePackages = "com.roles.rbacserver")
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> illegalTokenExceptionHandler(IllegalTokenException e) {
        log.error(e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> noSuchURIExceptionHandler(NoSuchURIException e) {
        log.error(e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error(e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> noSuchElementExceptionHandler(NoSuchElementException e) {
        log.error(e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> businessExceptionHandler(BusinessException e) {
        log.error(e.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
        log.error(e.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }


    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), message, LocalDateTime.now());
        return ResponseEntity.status(status).body(errorResponse);
    }

    record ErrorResponse(
            int statusCode,
            String message,
            LocalDateTime timestamp
    ) {
    }
}
