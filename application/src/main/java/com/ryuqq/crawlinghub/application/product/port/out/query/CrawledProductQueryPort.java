package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import java.util.Optional;

/**
 * CrawledProduct 조회 Port (Port Out - Query)
 *
 * <p>Application Service에서 사용됩니다.
 *
 * <p>조회 전용 Port로, 트랜잭션 없이 읽기 작업만 수행합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductQueryPort {

    /**
     * ID로 CrawledProduct 단건 조회
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
     * Seller ID와 Item No로 CrawledProduct 조회 (soft-delete 포함)
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return CrawledProduct (Optional, soft-deleted 포함)
     */
    Optional<CrawledProduct> findBySellerIdAndItemNoIncludingDeleted(
            SellerId sellerId, long itemNo);

    /**
     * Seller ID로 CrawledProduct 목록 조회
     *
     * @param sellerId 판매자 ID
     * @return CrawledProduct 목록
     */
    List<CrawledProduct> findBySellerId(SellerId sellerId);

    /**
     * 외부 동기화가 필요한 상품 목록 조회
     *
     * <p>needsSync가 true이고 모든 크롤링이 완료된 상품
     *
     * @param limit 조회 개수 제한
     * @return 동기화가 필요한 CrawledProduct 목록
     */
    List<CrawledProduct> findNeedsSyncProducts(int limit);

    /**
     * Seller ID와 Item No로 존재 여부 확인
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return 존재하면 true
     */
    boolean existsBySellerIdAndItemNo(SellerId sellerId, long itemNo);

    /**
     * 셀러별 CrawledProduct 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 해당 셀러의 상품 개수
     */
    long countBySellerId(SellerId sellerId);
}
