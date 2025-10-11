package com.ryuqq.crawlinghub.adapter.in.web.common;

/**
 * Centralized error code constants
 * Ensures consistency and prevents typos in error responses
 */
public final class ErrorCode {

    private ErrorCode() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // Business rule violations
    public static final String DUPLICATE_SITE = "DUPLICATE_SITE";
    public static final String SITE_NOT_FOUND = "SITE_NOT_FOUND";

    // Validation errors
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";

    // System errors
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
