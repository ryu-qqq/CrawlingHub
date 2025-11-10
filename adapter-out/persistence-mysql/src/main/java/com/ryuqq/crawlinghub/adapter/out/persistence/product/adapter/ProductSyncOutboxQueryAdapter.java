package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSyncOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ProductSyncOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ProductSyncOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.LoadProductSyncOutboxPort;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.SyncStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ProductSyncOutbox Query Adapter (CQRS - Query)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전담</li>
 *   <li>✅ LoadProductSyncOutboxPort 구현</li>
 *   <li>✅ Entity → Domain 변환</li>
 *   <li>✅ JPA Repository 호출</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSyncOutboxQueryAdapter implements LoadProductSyncOutboxPort {

    private final ProductSyncOutboxJpaRepository repository;
    private final ProductSyncOutboxMapper mapper;

    public ProductSyncOutboxQueryAdapter(
        ProductSyncOutboxJpaRepository repository,
        ProductSyncOutboxMapper mapper
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public java.util.Optional<ProductSyncOutbox> findById(Long outboxId) {
        Objects.requireNonNull(outboxId, "outboxId must not be null");

        return repository.findById(outboxId)
            .map(mapper::toDomain);
    }

    @Override
    public List<ProductSyncOutbox> findByStatus(SyncStatus status, int limit) {
        Objects.requireNonNull(status, "status must not be null");

        ProductSyncOutboxEntity.SyncStatus entityStatus = toEntityStatus(status);
        List<ProductSyncOutboxEntity> entities = repository.findByStatusOrderByCreatedAtAsc(entityStatus);

        return entities.stream()
            .limit(limit)
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    private ProductSyncOutboxEntity.SyncStatus toEntityStatus(SyncStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> ProductSyncOutboxEntity.SyncStatus.PENDING;
            case PROCESSING -> ProductSyncOutboxEntity.SyncStatus.PROCESSING;
            case COMPLETED -> ProductSyncOutboxEntity.SyncStatus.COMPLETED;
            case FAILED -> ProductSyncOutboxEntity.SyncStatus.FAILED;
        };
    }
}

