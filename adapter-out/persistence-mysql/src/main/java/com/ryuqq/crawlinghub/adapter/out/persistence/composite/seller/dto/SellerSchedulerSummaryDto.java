package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto;

/**
 * 셀러 연관 스케줄러 요약 DTO
 *
 * <p>QueryDSL Projections.constructor 용 Persistence 레이어 전용 DTO
 *
 * @param schedulerId 스케줄러 ID
 * @param schedulerName 스케줄러 이름
 * @param status 스케줄러 상태
 * @param cronExpression 크론 표현식
 * @author development-team
 * @since 1.0.0
 */
public record SellerSchedulerSummaryDto(
        Long schedulerId, String schedulerName, String status, String cronExpression) {}
