package com.ryuqq.crawlinghub.domain.eventbridge.vo;

/** Scheduler Aggregate Identifier. */
public record SchedulerId(Long value) {

    private static final SchedulerId NEW = new SchedulerId(null);

    public SchedulerId(Long value) {
        this.value = value;
    }

    public static SchedulerId forNew() {
        return NEW;
    }

    public static SchedulerId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("schedulerId must not be null");
        }
        return new SchedulerId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
