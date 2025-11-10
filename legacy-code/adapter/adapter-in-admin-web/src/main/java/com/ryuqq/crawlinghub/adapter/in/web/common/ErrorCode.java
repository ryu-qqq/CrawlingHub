package com.ryuqq.crawlinghub.adapter.in.web.common;

/**
 * Centralized error code constants
 * Ensures consistency and prevents typos in error responses
 */
public final class ErrorCode {

    private ErrorCode() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // Business rule violations - Site
    public static final String DUPLICATE_SITE = "DUPLICATE_SITE";
    public static final String SITE_NOT_FOUND = "SITE_NOT_FOUND";

    // Business rule violations - Workflow
    public static final String WORKFLOW_NOT_FOUND = "WORKFLOW_NOT_FOUND";
    public static final String INVALID_WORKFLOW = "INVALID_WORKFLOW";

    // Validation errors
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";

    // System errors
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
