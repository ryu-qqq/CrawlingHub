package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.LocalDateTime;

/**
 * Seller Summary Response
 *
 * <p>셀러 목록 조회용 요약 응답 데이터
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러 이름
 * @param sellerName 셀러 이름
 * @param active 활성화 여부
 * @param createdAt 생성 시각
 * @author development-team
 * @since 1.0.0
 */
public record SellerSummaryResponse(
        Long sellerId,
        String mustItSellerName,
        String sellerName,
        boolean active,
        LocalDateTime createdAt) {}
