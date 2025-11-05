package com.ryuqq.crawlinghub.application.seller.port.out;

import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistory;

/**
 * SaveProductCountHistoryPort - 상품 수 이력 저장 포트 (Command)
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface SaveProductCountHistoryPort {

    /**
     * 상품 수 이력 저장
     *
     * @param history 이력 Domain 객체
     * @return 저장된 ProductCountHistory
     */
    ProductCountHistory saveHistory(ProductCountHistory history);
}


