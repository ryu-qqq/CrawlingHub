package com.ryuqq.crawlinghub.domain.schedule.vo;

/**
 * 크롤 스케줄러 수정 데이터
 *
 * <p>스케줄러 수정에 필요한 모든 필드를 non-null로 포함합니다.
 *
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 */
public record CrawlSchedulerUpdateData(
        SchedulerName schedulerName, CronExpression cronExpression, SchedulerStatus status) {

    public static CrawlSchedulerUpdateData of(
            SchedulerName schedulerName, CronExpression cronExpression, SchedulerStatus status) {
        return new CrawlSchedulerUpdateData(schedulerName, cronExpression, status);
    }
}
