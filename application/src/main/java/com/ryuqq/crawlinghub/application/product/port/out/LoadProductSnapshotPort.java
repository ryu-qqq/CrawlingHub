package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.ProductSnapshot;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.util.Optional;

/**
 * ProductSnapshot 조회 Port (Outbound)
 *
 * <p>Query Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface LoadProductSnapshotPort {

    /**
     * ProductSnapshot ID로 조회
     */
    Optional<ProductSnapshot> findById(Long productId);

    /**
     * Mustit 상품 번호 + Seller ID로 조회
     */
    Optional<ProductSnapshot> findByMustitItemNoAndSellerId(Long mustItItemNo, MustitSellerId sellerId);
}

