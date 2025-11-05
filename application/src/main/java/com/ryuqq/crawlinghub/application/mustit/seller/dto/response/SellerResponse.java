package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import com.ryuqq.crawlinghub.domain.mustit.seller.SellerStatus;

import java.time.LocalDateTime;

/**
 * 셀러 정보 응답 DTO
 *
 * @param sellerId           셀러 ID
 * @param sellerCode         셀러 코드
 * @param sellerName         셀러 이름
 * @param status             상태
 * @param totalProductCount  총 상품 수
 * @param lastCrawledAt      마지막 크롤링 시간
 * @param createdAt          생성 시간
 * @param updatedAt          수정 시간
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record SellerResponse(
    Long sellerId,
    String sellerCode,
    String sellerName,
    SellerStatus status,
    Integer totalProductCount,
    LocalDateTime lastCrawledAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
