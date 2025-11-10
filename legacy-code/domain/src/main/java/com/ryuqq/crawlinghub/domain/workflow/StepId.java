package com.ryuqq.crawlinghub.domain.workflow;

public record StepId(Long value) {

    public StepId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Step ID must be positive: " + value);
        }
    }

    public static StepId of(Long value) {
        return new StepId(value);
    }

}
