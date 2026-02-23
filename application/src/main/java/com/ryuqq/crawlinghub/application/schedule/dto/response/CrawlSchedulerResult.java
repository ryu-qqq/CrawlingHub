package com.ryuqq.crawlinghub.application.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import java.time.Instant;

/**
 * 크롤 스케줄러 조회 결과 DTO (static from() 팩토리)
 *
 * @param id 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerResult(
        Long id,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * CrawlScheduler Aggregate → CrawlSchedulerResult 변환
     *
     * @param scheduler 크롤 스케줄러 Aggregate
     * @return CrawlSchedulerResult
     */
    public static CrawlSchedulerResult from(CrawlScheduler scheduler) {
        return new CrawlSchedulerResult(
                scheduler.getCrawlSchedulerIdValue(),
                scheduler.getSellerIdValue(),
                scheduler.getSchedulerNameValue(),
                scheduler.getCronExpressionValue(),
                scheduler.getStatus().name(),
                scheduler.getCreatedAt(),
                scheduler.getUpdatedAt());
    }
}
