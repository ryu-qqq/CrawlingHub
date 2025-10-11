package com.ryuqq.crawlinghub.domain.workflow;

public record WorkflowId(Long value) {

    public WorkflowId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Workflow ID must be positive: " + value);
        }
    }

    public static WorkflowId of(Long value) {
        return new WorkflowId(value);
    }

}
