package com.ryuqq.crawlinghub.application.seller.port.out;

import java.util.List;

import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistory;

/**
 * LoadProductCountHistoryPort - 상품 수 이력 조회 포트 (Query)
 *
 * <p>QueryDSL로 최적화된 조회 ⭐</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface LoadProductCountHistoryPort {

    /**
     * 셀러별 상품 수 이력 조회 (페이징)
     *
     * @param sellerId 셀러 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return ProductCountHistory 리스트
     */
    List<ProductCountHistory> loadHistories(MustItSellerId sellerId, int page, int size);

    /**
     * 전체 이력 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 전체 개수
     */
    long countHistories(MustItSellerId sellerId);
}

