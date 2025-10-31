package com.ryuqq.crawlinghub.application.mustit.seller.dto.query;

/**
 * 셀러 조회 Query
 *
 * @param sellerId 셀러 ID (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record GetSellerQuery(
    Long sellerId
) {
    public GetSellerQuery {
        // 검증 순서: null → 비즈니스 규칙
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 null일 수 없습니다");
        }
        if (sellerId <= 0) {
            throw new IllegalArgumentException("셀러 ID는 양수여야 합니다");
        }
    }
}
