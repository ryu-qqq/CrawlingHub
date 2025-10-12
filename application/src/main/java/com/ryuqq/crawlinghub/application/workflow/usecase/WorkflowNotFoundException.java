package com.ryuqq.crawlinghub.application.workflow.usecase;

/**
 * Exception thrown when a workflow is not found
 * Domain exception for workflow operations
 */
public class WorkflowNotFoundException extends RuntimeException {

    public WorkflowNotFoundException(String message) {
        super(message);
    }

    public WorkflowNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
