package com.ryuqq.crawlinghub.application.schedule.dto.response;

/**
 * 스케줄러 상세 조회 시 포함되는 셀러 요약 정보
 *
 * @param sellerId 셀러 ID
 * @param sellerName 셀러명
 * @param mustItSellerName MustIt 셀러명
 * @author development-team
 * @since 1.0.0
 */
public record SellerSummaryForScheduler(
        Long sellerId, String sellerName, String mustItSellerName) {}
