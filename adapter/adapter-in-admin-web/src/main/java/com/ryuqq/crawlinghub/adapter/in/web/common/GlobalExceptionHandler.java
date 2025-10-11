package com.ryuqq.crawlinghub.adapter.in.web.common;

import com.ryuqq.crawlinghub.application.site.usecase.DuplicateSiteException;
import com.ryuqq.crawlinghub.application.site.usecase.SiteNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers
 * Converts exceptions to standardized error responses
 * Architecture rules enforced by tests
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle business rule violation - duplicate site
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateSiteException.class)
    public ErrorResponse handleDuplicateSite(
            DuplicateSiteException ex,
            HttpServletRequest request) {

        return ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                ErrorCode.DUPLICATE_SITE,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Handle entity not found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SiteNotFoundException.class)
    public ErrorResponse handleSiteNotFound(
            SiteNotFoundException ex,
            HttpServletRequest request) {

        return ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                ErrorCode.SITE_NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Handle validation errors (@Valid)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR,
                message,
                request.getRequestURI()
        );
    }

    /**
     * Handle illegal arguments (e.g., invalid site type)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_ARGUMENT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Handle unexpected errors
     * Security: Logs full exception details but only returns generic message to client
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Log full exception details for debugging (server-side only)
        log.error("Unexpected error occurred at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Return generic message to client (no internal details exposed)
        return ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                request.getRequestURI()
        );
    }
}
