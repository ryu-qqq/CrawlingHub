package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ProductSnapshotMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ProductSnapshotJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.LoadProductSnapshotPort;
import com.ryuqq.crawlinghub.domain.product.ProductSnapshot;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * ProductSnapshot Query Adapter - CQRS Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (findById, findByMustitItemNoAndSellerId)</li>
 *   <li>✅ Entity → Domain 변환 (Mapper 사용)</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ ProductSnapshot Domain 반환</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Write 작업은 ProductSnapshotCommandAdapter에서 처리</li>
 *   <li>❌ Command 작업은 이 Adapter에서 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSnapshotQueryAdapter implements LoadProductSnapshotPort {

    private final ProductSnapshotJpaRepository jpaRepository;
    private final ProductSnapshotMapper mapper;

    /**
     * Adapter 생성자
     *
     * @param jpaRepository JPA Repository
     * @param mapper Entity → Domain Mapper
     */
    public ProductSnapshotQueryAdapter(
        ProductSnapshotJpaRepository jpaRepository,
        ProductSnapshotMapper mapper
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * ID로 ProductSnapshot 조회
     *
     * <p>Entity 조회 후 Domain으로 변환합니다.</p>
     *
     * @param productId 상품 ID (null 불가)
     * @return ProductSnapshot Domain (없으면 Optional.empty())
     * @throws IllegalArgumentException productId가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findById(Long productId) {
        Objects.requireNonNull(productId, "productId must not be null");

        return jpaRepository.findById(productId)
            .map(mapper::toDomain);
    }

    /**
     * Mustit 상품 번호 + Seller ID로 조회
     *
     * <p>Entity 조회 후 Domain으로 변환합니다.</p>
     *
     * @param mustItItemNo 머스트잇 상품 번호 (null 불가)
     * @param sellerId 셀러 ID (null 불가)
     * @return ProductSnapshot Domain (없으면 Optional.empty())
     * @throws IllegalArgumentException mustItItemNo 또는 sellerId가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findByMustitItemNoAndSellerId(Long mustItItemNo, MustItSellerId sellerId) {
        Objects.requireNonNull(mustItItemNo, "mustItItemNo must not be null");
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        return jpaRepository.findByMustitItemNoAndSellerId(mustItItemNo, sellerId.value())
            .map(mapper::toDomain);
    }
}

