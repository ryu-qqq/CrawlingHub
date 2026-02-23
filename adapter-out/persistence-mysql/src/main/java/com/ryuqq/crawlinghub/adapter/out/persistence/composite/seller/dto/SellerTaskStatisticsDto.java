package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto;

/**
 * 태스크 상태별 통계용 DTO (GROUP BY 결과)
 *
 * <p>QueryDSL Projections.constructor 용 Persistence 레이어 전용 DTO
 *
 * @param status 태스크 상태
 * @param count 해당 상태의 태스크 수
 * @author development-team
 * @since 1.0.0
 */
public record SellerTaskStatisticsDto(String status, long count) {}
