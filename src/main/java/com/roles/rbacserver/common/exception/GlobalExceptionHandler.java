package com.roles.rbacserver.common.exception;

import com.roles.rbacserver.common.application.dto.ApiCommonResponse;
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
    public ResponseEntity<ApiCommonResponse> illegalTokenExceptionHandler(IllegalTokenException e) {
        log.error(e.getMessage());
        return ApiCommonResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ApiCommonResponse> noSuchURIExceptionHandler(NoSuchURIException e) {
        log.error(e.getMessage());
        return ApiCommonResponse.of(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ApiCommonResponse> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error(e.getMessage());
        return ApiCommonResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ApiCommonResponse> noSuchElementExceptionHandler(NoSuchElementException e) {
        log.error(e.getMessage());
        return ApiCommonResponse.of(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ApiCommonResponse> businessExceptionHandler(BusinessException e) {
        log.error(e.getMessage());
        return ApiCommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ApiCommonResponse> exceptionHandler(Exception e) {
        log.error(e.getMessage());
        return ApiCommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
