package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * 셀러 기본 응답 DTO.
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerId 머스트잇 셀러 식별자
 * @param sellerName 셀러명
 * @param status 상태
 * @param createdAt 생성 시각
 */
public record SellerResponse(
    Long sellerId,
    String mustItSellerId,
    String sellerName,
    SellerStatus status,
    LocalDateTime createdAt
) {
}

