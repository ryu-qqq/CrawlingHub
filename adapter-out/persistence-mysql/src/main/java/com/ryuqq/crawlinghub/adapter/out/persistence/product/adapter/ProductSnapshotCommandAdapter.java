package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSnapshotEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ProductSnapshotMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ProductSnapshotJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.SaveProductSnapshotPort;
import com.ryuqq.crawlinghub.domain.product.ProductSnapshot;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ProductSnapshot Command Adapter (CQRS - Command)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ CUD (Create, Update, Delete) 작업 전담</li>
 *   <li>✅ SaveProductSnapshotPort 구현</li>
 *   <li>✅ Domain → Entity 변환</li>
 *   <li>✅ JPA Repository 호출</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSnapshotCommandAdapter implements SaveProductSnapshotPort {

    private final ProductSnapshotJpaRepository repository;
    private final ProductSnapshotMapper mapper;

    public ProductSnapshotCommandAdapter(
        ProductSnapshotJpaRepository repository,
        ProductSnapshotMapper mapper
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public ProductSnapshot save(ProductSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");

        // 1. Domain → Entity
        ProductSnapshotEntity entity = mapper.toEntity(snapshot);

        // 2. JPA save
        ProductSnapshotEntity savedEntity = repository.save(entity);

        // 3. Entity → Domain
        return mapper.toDomain(savedEntity);
    }
}

