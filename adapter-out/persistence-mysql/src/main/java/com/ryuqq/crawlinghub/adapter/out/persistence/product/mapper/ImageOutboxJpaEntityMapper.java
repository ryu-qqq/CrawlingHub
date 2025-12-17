package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ImageOutboxJpaEntity.OutboxStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * ImageOutboxJpaEntityMapper - Entity ↔ Domain 변환 Mapper
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
public class ImageOutboxJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain CrawledProductImageOutbox 도메인
     * @return ImageOutboxJpaEntity
     */
    public ImageOutboxJpaEntity toEntity(CrawledProductImageOutbox domain) {
        return ImageOutboxJpaEntity.of(
                domain.getId(),
                domain.getCrawledProductIdValue(),
                domain.getOriginalUrl(),
                toEntityImageType(domain.getImageType()),
                domain.getIdempotencyKey(),
                domain.getS3Url(),
                toEntityStatus(domain.getStatus()),
                domain.getRetryCount(),
                domain.getErrorMessage(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getProcessedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity ImageOutboxJpaEntity
     * @return CrawledProductImageOutbox 도메인
     */
    public CrawledProductImageOutbox toDomain(ImageOutboxJpaEntity entity) {
        return CrawledProductImageOutbox.reconstitute(
                entity.getId(),
                CrawledProductId.of(entity.getCrawledProductId()),
                entity.getOriginalUrl(),
                toDomainImageType(entity.getImageType()),
                entity.getIdempotencyKey(),
                entity.getS3Url(),
                toDomainStatus(entity.getStatus()),
                entity.getRetryCount(),
                entity.getErrorMessage(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getProcessedAt()));
    }

    // === Enum 변환 ===

    private ImageOutboxJpaEntity.ImageType toEntityImageType(ImageType domainType) {
        return switch (domainType) {
            case THUMBNAIL -> ImageOutboxJpaEntity.ImageType.THUMBNAIL;
            case DESCRIPTION -> ImageOutboxJpaEntity.ImageType.DESCRIPTION;
        };
    }

    private ImageType toDomainImageType(ImageOutboxJpaEntity.ImageType entityType) {
        return switch (entityType) {
            case THUMBNAIL -> ImageType.THUMBNAIL;
            case DESCRIPTION -> ImageType.DESCRIPTION;
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
