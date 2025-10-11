package com.ryuqq.crawlinghub.domain.execution;

public record StatisticsId(Long value) {

    public StatisticsId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Statistics ID must be positive: " + value);
        }
    }

    public static StatisticsId of(Long value) {
        return new StatisticsId(value);
    }

}
