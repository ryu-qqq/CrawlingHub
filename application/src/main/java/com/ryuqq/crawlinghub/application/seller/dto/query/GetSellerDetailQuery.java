package com.ryuqq.crawlinghub.application.seller.dto.query;

/**
 * 셀러 상세 조회 Query
 *
 * @param sellerId 셀러 ID
 * @author ryu-qqq
 * @since 2025-11-02
 */
public record GetSellerDetailQuery(
    Long sellerId
) {
    public GetSellerDetailQuery {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("셀러 ID는 필수이며 양수여야 합니다");
        }
    }
}
