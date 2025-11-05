package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.SellerBasicInfo;
import com.ryuqq.crawlinghub.domain.mustit.seller.SellerTimeInfo;
import org.springframework.stereotype.Component;

/**
 * MustitSeller Aggregate와 MustitSellerEntity 간 변환을 담당하는 Mapper
 * <p>
 * Domain 객체와 Persistence Entity 간의 양방향 변환을 제공합니다.
 * Domain의 CrawlIntervalType을 Entity에서 직접 사용하여 중복을 제거합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
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
        if (seller.getId() != null) {
            return MustitSellerEntity.reconstitute(
                    seller.getId(),
                    seller.getSellerId(),
                    seller.getName(),
                    seller.isActive(),
                    seller.getCrawlIntervalType(),
                    seller.getCrawlIntervalValue(),
                    seller.getCronExpression()
            );
        } else {
            return MustitSellerEntity.create(
                    seller.getSellerId(),
                    seller.getName(),
                    seller.isActive(),
                    seller.getCrawlIntervalType(),
                    seller.getCrawlIntervalValue(),
                    seller.getCronExpression()
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

        SellerBasicInfo basicInfo = SellerBasicInfo.of(
                entity.getId(),
                entity.getSellerId(),
                entity.getName(),
                entity.isActive()
        );

        CrawlInterval crawlInterval = new CrawlInterval(
                entity.getIntervalType(),
                entity.getIntervalValue()
        );

        SellerTimeInfo timeInfo = SellerTimeInfo.of(
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );

        return MustitSeller.reconstitute(basicInfo, crawlInterval, timeInfo);
    }
}
