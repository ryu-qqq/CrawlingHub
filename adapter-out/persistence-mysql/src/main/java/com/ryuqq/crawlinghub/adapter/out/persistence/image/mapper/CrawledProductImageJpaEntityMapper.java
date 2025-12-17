package com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.CrawledProductImageJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * CrawledProductImageJpaEntityMapper - 이미지 Entity ↔ Domain 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductImageJpaEntityMapper {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * Domain → Entity 변환
     *
     * @param domain CrawledProductImage
     * @return CrawledProductImageJpaEntity
     */
    public CrawledProductImageJpaEntity toEntity(CrawledProductImage domain) {
        return CrawledProductImageJpaEntity.of(
                domain.getId(),
                domain.getCrawledProductIdValue(),
                domain.getOriginalUrl(),
                domain.getS3Url(),
                domain.getFileAssetId(),
                domain.getImageType(),
                domain.getDisplayOrder(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getUpdatedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity CrawledProductImageJpaEntity
     * @return CrawledProductImage
     */
    public CrawledProductImage toDomain(CrawledProductImageJpaEntity entity) {
        return CrawledProductImage.reconstitute(
                entity.getId(),
                CrawledProductId.of(entity.getCrawledProductId()),
                entity.getOriginalUrl(),
                entity.getImageType(),
                entity.getDisplayOrder(),
                entity.getS3Url(),
                entity.getFileAssetId(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getUpdatedAt()));
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
