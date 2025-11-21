package com.ryuqq.crawlinghub.domain.schedule.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * 스케줄러 수정 이벤트
 *
 * <p><strong>용도</strong>: 스케줄러가 수정될 때 발행하여 AWS EventBridge에 동기화합니다.
 *
 * @param schedulerId 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @author development-team
 * @since 1.0.0
 */
public record SchedulerUpdatedEvent(
        CrawlSchedulerId schedulerId,
        SellerId sellerId,
        SchedulerName schedulerName,
        CronExpression cronExpression,
        SchedulerStatus status)
        implements DomainEvent {

    /** Compact Constructor (검증 로직) */
    public SchedulerUpdatedEvent {
        if (schedulerId == null) {
            throw new IllegalArgumentException("schedulerId는 null일 수 없습니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        if (schedulerName == null) {
            throw new IllegalArgumentException("schedulerName은 null일 수 없습니다.");
        }
        if (cronExpression == null) {
            throw new IllegalArgumentException("cronExpression은 null일 수 없습니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("status는 null일 수 없습니다.");
        }
    }

    /**
     * 팩토리 메서드 (도메인 규칙)
     *
     * @param schedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @return SchedulerUpdatedEvent
     */
    public static SchedulerUpdatedEvent of(
            CrawlSchedulerId schedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status) {
        return new SchedulerUpdatedEvent(
                schedulerId, sellerId, schedulerName, cronExpression, status);
    }
}
