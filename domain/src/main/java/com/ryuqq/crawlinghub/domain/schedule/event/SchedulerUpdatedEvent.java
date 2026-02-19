package com.ryuqq.crawlinghub.domain.schedule.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;

/**
 * 스케줄러 수정 이벤트
 *
 * <p>스케줄러가 수정될 때 발행하여 AWS EventBridge에 동기화합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record SchedulerUpdatedEvent(
        CrawlSchedulerId schedulerId,
        SellerId sellerId,
        SchedulerName schedulerName,
        CronExpression cronExpression,
        SchedulerStatus status,
        Instant occurredAt)
        implements DomainEvent {

    public static SchedulerUpdatedEvent of(
            CrawlSchedulerId schedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant occurredAt) {
        return new SchedulerUpdatedEvent(
                schedulerId, sellerId, schedulerName, cronExpression, status, occurredAt);
    }

    public Long getCrawlSchedulerIdValue() {
        return schedulerId.value();
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public String getSchedulerNameValue() {
        return schedulerName.value();
    }

    public String getCronExpressionValue() {
        return cronExpression.value();
    }
}
