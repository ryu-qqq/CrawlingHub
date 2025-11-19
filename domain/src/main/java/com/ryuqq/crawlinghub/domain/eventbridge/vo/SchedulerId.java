package com.ryuqq.crawlinghub.domain.eventbridge.vo;

/**
 * Scheduler Aggregate Identifier.
 */
public record SchedulerId(Long value) {

    public SchedulerId {
        if (value == null) {
            throw new IllegalArgumentException("schedulerId must not be null");
        }
    }

    public static SchedulerId of(Long value) {
        return new SchedulerId(value);
    }
}

