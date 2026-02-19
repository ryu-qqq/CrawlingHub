package com.ryuqq.crawlinghub.domain.schedule.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;

/**
 * 스케줄러 등록 이벤트
 *
 * <p>스케줄러가 등록될 때 발행하여 AWS EventBridge에 동기화합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record SchedulerRegisteredEvent(
        CrawlSchedulerId schedulerId,
        CrawlSchedulerHistoryId historyId,
        SellerId sellerId,
        SchedulerName schedulerName,
        CronExpression cronExpression,
        Instant occurredAt)
        implements DomainEvent {

    public static SchedulerRegisteredEvent of(
            CrawlSchedulerId schedulerId,
            CrawlSchedulerHistoryId historyId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            Instant occurredAt) {
        return new SchedulerRegisteredEvent(
                schedulerId, historyId, sellerId, schedulerName, cronExpression, occurredAt);
    }

    public String getScheduleNameValue() {
        return schedulerName.value();
    }

    public Long getCrawlSchedulerIdValue() {
        return schedulerId.value();
    }

    public Long getHistoryIdValue() {
        return historyId.value();
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }
}
