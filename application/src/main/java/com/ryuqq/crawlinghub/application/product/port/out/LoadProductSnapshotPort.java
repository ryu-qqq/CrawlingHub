package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.ProductSnapshot;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;

import java.util.Optional;

/**
 * ProductSnapshot 조회 Port (Outbound) - CQRS Query Port
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Query 전용 Port (읽기 전용)</li>
 *   <li>✅ ProductSnapshot Domain 반환</li>
 *   <li>✅ Adapter에서 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>구현체:</strong></p>
 * <ul>
 *   <li>ProductSnapshotQueryAdapter (Query Adapter)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface LoadProductSnapshotPort {

    /**
     * ProductSnapshot ID로 조회
     *
     * @param productId 상품 ID
     * @return ProductSnapshot Domain (없으면 Optional.empty())
     */
    Optional<ProductSnapshot> findById(Long productId);

    /**
     * Mustit 상품 번호 + Seller ID로 조회
     *
     * @param mustItItemNo 머스트잇 상품 번호
     * @param sellerId 셀러 ID
     * @return ProductSnapshot Domain (없으면 Optional.empty())
     */
    Optional<ProductSnapshot> findByMustitItemNoAndSellerId(Long mustItItemNo, MustItSellerId sellerId);
}

