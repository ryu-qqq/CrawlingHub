package com.ryuqq.crawlinghub.application.workflow.usecase;

/**
 * Exception thrown when workflow validation fails
 * Used for business rule violations such as:
 * - Invalid step order sequence
 * - Non-existent endpoint key references
 * - Circular output reference dependencies
 * - Invalid expression formats
 */
public class InvalidWorkflowException extends RuntimeException {

    public InvalidWorkflowException(String message) {
        super(message);
    }

    public InvalidWorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
