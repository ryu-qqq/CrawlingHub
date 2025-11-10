package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSyncOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ProductSyncOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ProductSyncOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.SaveProductSyncOutboxPort;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ProductSyncOutbox Command Adapter (CQRS - Command)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ CUD (Create, Update, Delete) 작업 전담</li>
 *   <li>✅ SaveProductSyncOutboxPort 구현</li>
 *   <li>✅ Domain → Entity 변환</li>
 *   <li>✅ JPA Repository 호출</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSyncOutboxCommandAdapter implements SaveProductSyncOutboxPort {

    private final ProductSyncOutboxJpaRepository repository;
    private final ProductSyncOutboxMapper mapper;

    public ProductSyncOutboxCommandAdapter(
        ProductSyncOutboxJpaRepository repository,
        ProductSyncOutboxMapper mapper
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public ProductSyncOutbox save(ProductSyncOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");

        // 1. Domain → Entity
        ProductSyncOutboxEntity entity = mapper.toEntity(outbox);

        // 2. JPA save
        ProductSyncOutboxEntity savedEntity = repository.save(entity);

        // 3. Entity → Domain
        return mapper.toDomain(savedEntity);
    }
}

