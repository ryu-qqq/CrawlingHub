package com.ryuqq.crawlinghub.domain.task;

public record TaskId(Long value) {

    public TaskId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Task ID must be positive: " + value);
        }
    }

    public static TaskId of(Long value) {
        return new TaskId(value);
    }

}
