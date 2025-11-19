package com.ryuqq.crawlinghub.application.scheduler.dto.response;

import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import java.time.LocalDateTime;

/**
 * SchedulerDetailResponse DTO
 */
public record SchedulerDetailResponse(
        Long schedulerId,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        SchedulerStatus status,
        String eventBridgeRuleName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SchedulerDetailResponse from(
            CrawlingScheduler scheduler,
            String eventBridgeRuleName
    ) {
        return new SchedulerDetailResponse(
                scheduler.getSchedulerId(),
                scheduler.getSellerId(),
                scheduler.getSchedulerName(),
                extractCronExpressionValue(scheduler.getCronExpression()),
                scheduler.getStatus(),
                eventBridgeRuleName,
                scheduler.getCreatedAt(),
                scheduler.getUpdatedAt()
        );
    }

    private static String extractCronExpressionValue(CronExpression cronExpression) {
        return cronExpression != null ? cronExpression.value() : null;
    }
}

