package com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto;

import java.time.LocalDateTime;

/**
 * Scheduler + Seller LEFT JOIN 결과 DTO
 *
 * <p>QueryDSL Projections.constructor 용 Persistence 레이어 전용 DTO
 *
 * @param schedulerId 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @param schedulerCreatedAt 스케줄러 생성 시각
 * @param schedulerUpdatedAt 스케줄러 수정 시각
 * @param sellerName 셀러명 (nullable, LEFT JOIN)
 * @param mustItSellerName 머스트잇 셀러명 (nullable, LEFT JOIN)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerCompositeDto(
        Long schedulerId,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        String status,
        LocalDateTime schedulerCreatedAt,
        LocalDateTime schedulerUpdatedAt,
        String sellerName,
        String mustItSellerName) {}
