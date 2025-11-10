package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ProductSnapshotMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ProductSnapshotJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.LoadProductSnapshotPort;
import com.ryuqq.crawlinghub.domain.product.ProductSnapshot;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * ProductSnapshot Query Adapter (CQRS - Query)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전담</li>
 *   <li>✅ LoadProductSnapshotPort 구현</li>
 *   <li>✅ Entity → Domain 변환</li>
 *   <li>✅ JPA Repository 호출</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSnapshotQueryAdapter implements LoadProductSnapshotPort {

    private final ProductSnapshotJpaRepository repository;
    private final ProductSnapshotMapper mapper;

    public ProductSnapshotQueryAdapter(
        ProductSnapshotJpaRepository repository,
        ProductSnapshotMapper mapper
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public Optional<ProductSnapshot> findById(Long productId) {
        Objects.requireNonNull(productId, "productId must not be null");

        return repository.findById(productId)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<ProductSnapshot> findByMustitItemNoAndSellerId(Long mustItItemNo, MustitSellerId sellerId) {
        Objects.requireNonNull(mustItItemNo, "mustItItemNo must not be null");
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        return repository.findByMustItItemNoAndSellerId(mustItItemNo, sellerId.value())
            .map(mapper::toDomain);
    }
}

