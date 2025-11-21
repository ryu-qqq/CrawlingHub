package com.ryuqq.crawlinghub.application.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

/** Scheduler 요약 응답 DTO */
public record SchedulerSummaryResponse(
        Long schedulerId,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        SchedulerStatus status) {

    public static SchedulerSummaryResponse from(CrawlingScheduler scheduler) {
        return new SchedulerSummaryResponse(
                scheduler.getSchedulerId(),
                scheduler.getSellerId(),
                scheduler.getSchedulerName(),
                extractCronExpressionValue(scheduler.getCronExpression()),
                scheduler.getStatus());
    }

    private static String extractCronExpressionValue(CronExpression cronExpression) {
        return cronExpression != null ? cronExpression.value() : null;
    }
}
