package com.ryuqq.crawlinghub.domain.task;

public record AttemptId(Long value) {

    public AttemptId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Attempt ID must be positive: " + value);
        }
    }

    public static AttemptId of(Long value) {
        return new AttemptId(value);
    }

}
