package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSyncOutboxEntity;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.SyncStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ProductSyncOutbox Mapper (Pure Java)
 *
 * <p>Entity ↔ Domain 변환을 담당합니다.</p>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ MapStruct 금지 - Pure Java 사용</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ Value Object 수동 변환</li>
 *   <li>✅ Null 체크 및 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSyncOutboxMapper {

    /**
     * Domain → Entity 변환
     *
     * <p>Domain Model의 ID가 null이면 신규 생성 Entity 반환</p>
     */
    public ProductSyncOutboxEntity toEntity(ProductSyncOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");

        Long id = outbox.getIdValue();
        Long productId = outbox.getProductId();
        String productJson = outbox.getProductJson();
        SyncStatus domainStatus = outbox.getStatus();
        ProductSyncOutboxEntity.SyncStatus entityStatus = toEntityStatus(domainStatus);

        if (id == null) {
            // 신규 생성 Entity
            return ProductSyncOutboxEntity.create(productId, productJson);
        } else {
            // DB reconstitute Entity
            return ProductSyncOutboxEntity.reconstitute(
                id,
                productId,
                productJson,
                entityStatus,
                outbox.getRetryCount(),
                outbox.getErrorMessage(),
                outbox.getProcessedAt(),
                outbox.getCreatedAt(),
                outbox.getCreatedAt() // updatedAt이 없으므로 createdAt 사용
            );
        }
    }

    /**
     * Entity → Domain 변환
     */
    public ProductSyncOutbox toDomain(ProductSyncOutboxEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return ProductSyncOutbox.reconstitute(
            ProductSyncOutboxId.of(entity.getId()),
            entity.getProductId(),
            entity.getProductJson(),
            toDomainStatus(entity.getStatus()),
            entity.getRetryCount(),
            entity.getErrorMessage(),
            entity.getCreatedAt(),
            entity.getProcessedAt()
        );
    }

    /**
     * Domain SyncStatus → Entity SyncStatus
     */
    private ProductSyncOutboxEntity.SyncStatus toEntityStatus(SyncStatus domainStatus) {
        Objects.requireNonNull(domainStatus, "domainStatus must not be null");

        return switch (domainStatus) {
            case PENDING -> ProductSyncOutboxEntity.SyncStatus.PENDING;
            case PROCESSING -> ProductSyncOutboxEntity.SyncStatus.PROCESSING;
            case COMPLETED -> ProductSyncOutboxEntity.SyncStatus.COMPLETED;
            case FAILED -> ProductSyncOutboxEntity.SyncStatus.FAILED;
        };
    }

    /**
     * Entity SyncStatus → Domain SyncStatus
     */
    private SyncStatus toDomainStatus(ProductSyncOutboxEntity.SyncStatus entityStatus) {
        Objects.requireNonNull(entityStatus, "entityStatus must not be null");

        return switch (entityStatus) {
            case PENDING -> SyncStatus.PENDING;
            case PROCESSING -> SyncStatus.PROCESSING;
            case COMPLETED -> SyncStatus.COMPLETED;
            case FAILED -> SyncStatus.FAILED;
        };
    }
}

