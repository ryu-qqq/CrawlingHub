package com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.ProductCountHistoryEntity;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistory;
import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistoryId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ProductCountHistoryMapper - Entity ↔ Domain 변환
 * <p>
 * Domain Model ↔ JPA Entity 변환을 담당합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class ProductCountHistoryMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain Domain 객체
     * @return Entity
     * @throws IllegalArgumentException domain이 null인 경우
     */
    public ProductCountHistoryEntity toEntity(ProductCountHistory domain) {
        Objects.requireNonNull(domain, "domain must not be null");

        if (domain.getId() != null) {
            // Domain 객체에는 createdAt, updatedAt이 없으므로
            // Entity 조회 후 해당 필드를 사용하거나, 현재 시각으로 설정
            // reconstitute는 DB에서 조회된 Entity에만 사용되므로
            // toEntity는 create만 사용
            throw new UnsupportedOperationException(
                "Domain 객체는 create만 지원합니다. reconstitute는 DB 조회 후 사용하세요."
            );
        } else {
            return ProductCountHistoryEntity.create(
                domain.getSellerId().value(),
                domain.getProductCount(),
                domain.getExecutedDate()
            );
        }
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity Entity
     * @return Domain 객체
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public ProductCountHistory toDomain(ProductCountHistoryEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return ProductCountHistory.reconstitute(
            entity.getId() != null ? ProductCountHistoryId.of(entity.getId()) : null,
            MustItSellerId.of(entity.getSellerId()),
            entity.getProductCount(),
            entity.getExecutedDate()
        );
    }
}

