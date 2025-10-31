package com.ryuqq.crawlinghub.application.product.sync.port.out;

import com.ryuqq.crawlinghub.domain.product.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.ProductId;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상품 조회 Port
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface LoadProductPort {

    /**
     * ID로 상품 조회
     */
    Optional<CrawledProduct> findById(ProductId id);

    /**
     * 셀러의 모든 완료 상품 조회
     */
    List<CrawledProduct> findCompletedProductsBySellerId(MustitSellerId sellerId);

    /**
     * 특정 시간 이후 변경된 완료 상품 조회
     */
    List<CrawledProduct> findChangedProductsAfter(LocalDateTime after, int limit);

    /**
     * 셀러 + 시간 필터 조회
     */
    List<CrawledProduct> findChangedProductsBySellerAfter(
        MustitSellerId sellerId,
        LocalDateTime after,
        int limit
    );
}
