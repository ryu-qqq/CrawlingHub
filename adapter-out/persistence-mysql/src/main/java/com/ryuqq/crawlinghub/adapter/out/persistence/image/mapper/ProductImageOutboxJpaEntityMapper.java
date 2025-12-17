package com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * ProductImageOutboxJpaEntityMapper - Outbox Entity ↔ Domain 변환
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 ProductImageOutbox 간 변환을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductImageOutboxJpaEntityMapper {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * Domain → Entity 변환
     *
     * @param domain ProductImageOutbox
     * @return ProductImageOutboxJpaEntity
     */
    public ProductImageOutboxJpaEntity toEntity(ProductImageOutbox domain) {
        return ProductImageOutboxJpaEntity.of(
                domain.getId(),
                domain.getCrawledProductImageId(),
                domain.getIdempotencyKey(),
                domain.getStatus(),
                domain.getRetryCount(),
                domain.getErrorMessage(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getProcessedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity ProductImageOutboxJpaEntity
     * @return ProductImageOutbox
     */
    public ProductImageOutbox toDomain(ProductImageOutboxJpaEntity entity) {
        return ProductImageOutbox.reconstitute(
                entity.getId(),
                entity.getCrawledProductImageId(),
                entity.getIdempotencyKey(),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getErrorMessage(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getProcessedAt()));
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(DEFAULT_ZONE).toInstant();
    }
}
