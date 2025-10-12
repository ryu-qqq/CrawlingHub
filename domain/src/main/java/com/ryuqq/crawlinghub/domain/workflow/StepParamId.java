package com.ryuqq.crawlinghub.domain.workflow;

public record StepParamId(Long value) {

    public StepParamId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Step Param ID must be positive: " + value);
        }
    }

    public static StepParamId of(Long value) {
        return new StepParamId(value);
    }

}
