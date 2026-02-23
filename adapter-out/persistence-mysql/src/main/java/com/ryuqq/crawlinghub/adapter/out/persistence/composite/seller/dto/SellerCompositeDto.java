package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto;

import java.time.LocalDateTime;

/**
 * Seller 기본 정보 DTO
 *
 * <p>QueryDSL Projections.constructor 용 Persistence 레이어 전용 DTO
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러명
 * @param sellerName 커머스 셀러명
 * @param status 셀러 상태
 * @param productCount 상품 수
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record SellerCompositeDto(
        Long sellerId,
        String mustItSellerName,
        String sellerName,
        String status,
        int productCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
