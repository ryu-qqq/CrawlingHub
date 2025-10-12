package com.ryuqq.crawlinghub.domain.workflow;

public record StepOutputId(Long value) {

    public StepOutputId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Step Output ID must be positive: " + value);
        }
    }

    public static StepOutputId of(Long value) {
        return new StepOutputId(value);
    }

}
