package com.ryuqq.crawlinghub.application.mustit.seller.dto.query;

import com.ryuqq.crawlinghub.domain.mustit.seller.SellerStatus;

/**
 * 셀러 목록 조회 Query
 *
 * @param status 셀러 상태 필터 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author ryu-qqq
 * @since 2025-11-02
 */
public record GetSellersQuery(
    SellerStatus status,
    int page,
    int size
) {
    public GetSellersQuery {
        // 검증 순서: 비즈니스 규칙
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다");
        }
    }

    /**
     * 기본값으로 생성 (첫 페이지, 20개 크기)
     */
    public static GetSellersQuery ofDefault() {
        return new GetSellersQuery(null, 0, 20);
    }
}
