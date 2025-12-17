package com.ryuqq.crawlinghub.application.product.port.in.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;

/**
 * CrawledProduct 조회 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetCrawledProductUseCase {

    /**
     * ID로 CrawledProduct 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return CrawledProduct (Optional)
     */
    Optional<CrawledProduct> findById(CrawledProductId crawledProductId);

    /**
     * Seller ID와 Item No로 CrawledProduct 조회
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return CrawledProduct (Optional)
     */
    Optional<CrawledProduct> findBySellerIdAndItemNo(SellerId sellerId, long itemNo);

    /**
     * Seller ID로 CrawledProduct 목록 조회
     *
     * @param sellerId 판매자 ID
     * @return CrawledProduct 목록
     */
    List<CrawledProduct> findBySellerId(SellerId sellerId);
}
