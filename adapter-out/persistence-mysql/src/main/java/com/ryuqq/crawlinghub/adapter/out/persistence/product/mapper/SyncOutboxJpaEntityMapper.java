package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity.OutboxStatus;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity.SyncType;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * SyncOutboxJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 Domain 객체 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>LocalDateTime ↔ Instant 변환
 *   <li>Entity Enum ↔ Domain Enum 변환
 *   <li>Long FK ↔ Value Object 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SyncOutboxJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain CrawledProductSyncOutbox 도메인
     * @return SyncOutboxJpaEntity
     */
    public SyncOutboxJpaEntity toEntity(CrawledProductSyncOutbox domain) {
        return SyncOutboxJpaEntity.of(
                domain.getId(),
                domain.getCrawledProductIdValue(),
                domain.getSellerIdValue(),
                domain.getItemNo(),
                toEntitySyncType(domain.getSyncType()),
                domain.getIdempotencyKey(),
                domain.getExternalProductId(),
                toEntityStatus(domain.getStatus()),
                domain.getRetryCount(),
                domain.getErrorMessage(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getProcessedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity SyncOutboxJpaEntity
     * @return CrawledProductSyncOutbox 도메인
     */
    public CrawledProductSyncOutbox toDomain(SyncOutboxJpaEntity entity) {
        return CrawledProductSyncOutbox.reconstitute(
                entity.getId(),
                CrawledProductId.of(entity.getCrawledProductId()),
                SellerId.of(entity.getSellerId()),
                entity.getItemNo(),
                toDomainSyncType(entity.getSyncType()),
                entity.getIdempotencyKey(),
                entity.getExternalProductId(),
                toDomainStatus(entity.getStatus()),
                entity.getRetryCount(),
                entity.getErrorMessage(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getProcessedAt()));
    }

    // === Enum 변환 ===

    private SyncType toEntitySyncType(CrawledProductSyncOutbox.SyncType domainType) {
        return switch (domainType) {
            case CREATE -> SyncType.CREATE;
            case UPDATE -> SyncType.UPDATE;
        };
    }

    private CrawledProductSyncOutbox.SyncType toDomainSyncType(SyncType entityType) {
        return switch (entityType) {
            case CREATE -> CrawledProductSyncOutbox.SyncType.CREATE;
            case UPDATE -> CrawledProductSyncOutbox.SyncType.UPDATE;
        };
    }

    private OutboxStatus toEntityStatus(ProductOutboxStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> OutboxStatus.PENDING;
            case PROCESSING -> OutboxStatus.PROCESSING;
            case COMPLETED -> OutboxStatus.COMPLETED;
            case FAILED -> OutboxStatus.FAILED;
        };
    }

    private ProductOutboxStatus toDomainStatus(OutboxStatus entityStatus) {
        return switch (entityStatus) {
            case PENDING -> ProductOutboxStatus.PENDING;
            case PROCESSING -> ProductOutboxStatus.PROCESSING;
            case COMPLETED -> ProductOutboxStatus.COMPLETED;
            case FAILED -> ProductOutboxStatus.FAILED;
        };
    }

    // === 시간 변환 ===

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
