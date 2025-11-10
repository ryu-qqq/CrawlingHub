package com.ryuqq.crawlinghub.application.execution.usecase;

/**
 * Exception thrown when an execution is not found
 */
public class ExecutionNotFoundException extends RuntimeException {

    private final Long executionId;

    public ExecutionNotFoundException(Long executionId) {
        super("Execution not found with ID: " + executionId);
        this.executionId = executionId;
    }

    public Long getExecutionId() {
        return executionId;
    }
}
