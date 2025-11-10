package com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import org.springframework.stereotype.Component;

/**
 * MustitSeller Aggregate와 MustitSellerEntity 간 변환을 담당하는 Mapper
 * <p>
 * Domain 객체와 Persistence Entity 간의 양방향 변환을 제공합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class MustitSellerMapper {

    /**
     * Domain Aggregate → JPA Entity 변환
     *
     * @param seller Domain Aggregate
     * @return JPA Entity
     * @throws IllegalArgumentException seller가 null인 경우
     */
    public MustitSellerEntity toEntity(MustitSeller seller) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }

        // Entity ID가 있으면 reconstitute, 없으면 create
        if (seller.getIdValue() != null) {
            return MustitSellerEntity.reconstitute(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerNameValue(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt()
            );
        } else {
            return MustitSellerEntity.create(
                    seller.getSellerCode(),
                    seller.getSellerNameValue(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt()
            );
        }
    }

    /**
     * JPA Entity → Domain Aggregate 변환
     *
     * @param entity JPA Entity
     * @return Domain Aggregate
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public MustitSeller toDomain(MustitSellerEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }

        return MustitSeller.reconstitute(
                MustitSellerId.of(entity.getId()),
                entity.getSellerCode(),
                entity.getSellerName(),
                entity.getStatus(),
                entity.getTotalProductCount(),
                entity.getLastCrawledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
