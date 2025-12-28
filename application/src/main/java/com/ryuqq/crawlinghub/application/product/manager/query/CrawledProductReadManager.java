package com.ryuqq.crawlinghub.application.product.manager.query;

import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProduct 조회 관리자
 *
 * <p>CrawledProductQueryPort를 래핑하여 조회 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductReadManager {

    private final CrawledProductQueryPort crawledProductQueryPort;

    public CrawledProductReadManager(CrawledProductQueryPort crawledProductQueryPort) {
        this.crawledProductQueryPort = crawledProductQueryPort;
    }

    /**
     * ID로 CrawledProduct 단건 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return CrawledProduct (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProduct> findById(CrawledProductId crawledProductId) {
        return crawledProductQueryPort.findById(crawledProductId);
    }

    /**
     * Seller ID와 Item No로 CrawledProduct 조회
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return CrawledProduct (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProduct> findBySellerIdAndItemNo(SellerId sellerId, long itemNo) {
        return crawledProductQueryPort.findBySellerIdAndItemNo(sellerId, itemNo);
    }

    /**
     * Seller ID로 CrawledProduct 목록 조회
     *
     * @param sellerId 판매자 ID
     * @return CrawledProduct 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProduct> findBySellerId(SellerId sellerId) {
        return crawledProductQueryPort.findBySellerId(sellerId);
    }

    /**
     * 외부 동기화가 필요한 상품 목록 조회
     *
     * @param limit 조회 개수 제한
     * @return 동기화가 필요한 CrawledProduct 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProduct> findNeedsSyncProducts(int limit) {
        return crawledProductQueryPort.findNeedsSyncProducts(limit);
    }

    /**
     * Seller ID와 Item No로 존재 여부 확인
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return 존재하면 true
     */
    @Transactional(readOnly = true)
    public boolean existsBySellerIdAndItemNo(SellerId sellerId, long itemNo) {
        return crawledProductQueryPort.existsBySellerIdAndItemNo(sellerId, itemNo);
    }

    /**
     * 셀러별 CrawledProduct 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 해당 셀러의 상품 개수
     */
    public long countBySellerId(SellerId sellerId) {
        return crawledProductQueryPort.countBySellerId(sellerId);
    }
}
