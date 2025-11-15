package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ProductSnapshot JPA Repository
 *
 * <p><strong>CQRS 패턴 적용</strong></p>
 * <ul>
 *   <li>✅ Command: save(), delete()</li>
 *   <li>✅ Query: findById(), findByMustitItemNoAndSellerId() (간단한 조회만 허용)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface ProductSnapshotJpaRepository extends JpaRepository<ProductSnapshotEntity, Long> {

    /**
     * Mustit 상품 번호 + Seller ID로 조회
     *
     * @param mustitItemNo Mustit 상품 번호
     * @param sellerId Seller ID
     * @return ProductSnapshot Entity
     */
    Optional<ProductSnapshotEntity> findByMustitItemNoAndSellerId(Long mustitItemNo, Long sellerId);
}

