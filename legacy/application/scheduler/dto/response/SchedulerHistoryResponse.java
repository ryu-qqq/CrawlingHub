package com.ryuqq.crawlinghub.application.schedule.dto.response;

import java.time.LocalDateTime;

/** SchedulerHistory 응답 DTO */
public record SchedulerHistoryResponse(
        Long historyId,
        Long schedulerId,
        String attributeName,
        String previousValue,
        String currentValue,
        LocalDateTime occurredAt) {

    public static SchedulerHistoryResponse of(
            Long historyId,
            Long schedulerId,
            String attributeName,
            String previousValue,
            String currentValue,
            LocalDateTime occurredAt) {
        return new SchedulerHistoryResponse(
                historyId, schedulerId, attributeName, previousValue, currentValue, occurredAt);
    }
}
