package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.Instant;

/**
 * Seller Response
 *
 * <p>셀러 상세 응답 데이터 (등록/수정 결과)
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러 이름
 * @param sellerName 셀러 이름
 * @param active 활성화 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record SellerResponse(
        Long sellerId,
        String mustItSellerName,
        String sellerName,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {}
