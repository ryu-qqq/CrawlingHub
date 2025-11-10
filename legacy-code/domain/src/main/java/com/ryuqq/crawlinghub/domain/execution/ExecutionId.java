package com.ryuqq.crawlinghub.domain.execution;

public record ExecutionId(Long value) {

    public ExecutionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Execution ID must be positive: " + value);
        }
    }

    public static ExecutionId of(Long value) {
        return new ExecutionId(value);
    }

}
