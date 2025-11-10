package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.ProductId;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.util.Optional;

/**
 * Product 조회 Port (Outbound)
 *
 * <p>Query Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface LoadProductPort {

    /**
     * Product ID로 조회
     */
    Optional<CrawledProduct> findById(ProductId productId);

    /**
     * Mustit 상품 번호 + Seller ID로 조회
     *
     * <p>최초 크롤링 판단용
     */
    Optional<CrawledProduct> findByMustitItemNoAndSellerId(Long mustItItemNo, MustitSellerId sellerId);

    /**
     * 최초 크롤링 여부 확인
     */
    boolean existsByMustitItemNoAndSellerId(Long mustItItemNo, MustitSellerId sellerId);
}
