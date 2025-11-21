package com.ryuqq.crawlinghub.domain.eventbridge.history;

import java.time.LocalDateTime;

/** Scheduler 변경 이력 */
public record SchedulerHistory(
        Long historyId,
        Long schedulerId,
        String attributeName,
        String previousValue,
        String currentValue,
        LocalDateTime occurredAt) {

    public static SchedulerHistory of(
            Long historyId,
            Long schedulerId,
            String attributeName,
            String previousValue,
            String currentValue,
            LocalDateTime occurredAt) {
        return new SchedulerHistory(
                historyId, schedulerId, attributeName, previousValue, currentValue, occurredAt);
    }
}
