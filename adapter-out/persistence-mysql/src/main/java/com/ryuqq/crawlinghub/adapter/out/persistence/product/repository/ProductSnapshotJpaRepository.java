package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ProductSnapshot JPA Repository
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Spring Data JPA 사용</li>
 *   <li>✅ Query 메서드 네이밍 규칙 준수</li>
 *   <li>✅ Optional 반환 (findBy...)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface ProductSnapshotJpaRepository extends JpaRepository<ProductSnapshotEntity, Long> {

    /**
     * Mustit 상품 번호 + Seller ID로 조회
     *
     * @param mustItItemNo 머스트잇 상품 번호
     * @param sellerId 셀러 ID (Long FK)
     * @return ProductSnapshot (Optional)
     */
    Optional<ProductSnapshotEntity> findByMustItItemNoAndSellerId(Long mustItItemNo, Long sellerId);
}

