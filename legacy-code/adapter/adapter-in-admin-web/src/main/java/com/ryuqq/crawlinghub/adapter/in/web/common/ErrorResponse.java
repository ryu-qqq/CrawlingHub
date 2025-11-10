package com.ryuqq.crawlinghub.adapter.in.web.common;

import java.time.LocalDateTime;

/**
 * Standardized error response DTO
 * Must be a Java record (enforced by architecture tests)
 *
 * @param timestamp when the error occurred
 * @param status HTTP status code
 * @param error error type/code
 * @param message human-readable error message
 * @param path the request path that caused the error
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {

    /**
     * Create error response with current timestamp
     *
     * @param status HTTP status code
     * @param error error type
     * @param message error message
     * @param path request path
     * @return error response
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path
        );
    }
}
