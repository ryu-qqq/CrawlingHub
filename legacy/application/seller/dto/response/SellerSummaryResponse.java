package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * 셀러 요약 응답 DTO.
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerId 머스트잇 셀러 식별자
 * @param sellerName 셀러명
 * @param status 상태
 * @param totalSchedulerCount 총 스케줄 수
 */
public record SellerSummaryResponse(
        Long sellerId,
        String mustItSellerId,
        String sellerName,
        SellerStatus status,
        Integer totalSchedulerCount) {}
