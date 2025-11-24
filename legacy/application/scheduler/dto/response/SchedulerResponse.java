package com.ryuqq.crawlinghub.application.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import java.time.LocalDateTime;

/**
 * SchedulerResponse DTO (Application Layer)
 *
 * <p>CQRS-Query 용 응답 DTO로, Domain Aggregate 로부터 필요한 정보만 추출하여 전달한다.
 */
public record SchedulerResponse(
        Long schedulerId,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        SchedulerStatus status,
        String eventBridgeRuleName,
        LocalDateTime createdAt) {

    public static SchedulerResponse from(CrawlingScheduler scheduler, String eventBridgeRuleName) {
        return new SchedulerResponse(
                scheduler.getSchedulerId(),
                scheduler.getSellerId(),
                scheduler.getSchedulerName(),
                extractCronExpressionValue(scheduler.getCronExpression()),
                scheduler.getStatus(),
                eventBridgeRuleName,
                scheduler.getCreatedAt());
    }

    private static String extractCronExpressionValue(CronExpression cronExpression) {
        return cronExpression != null ? cronExpression.value() : null;
    }
}
