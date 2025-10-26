package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.SellerBasicInfo;
import com.ryuqq.crawlinghub.domain.mustit.seller.SellerTimeInfo;
import org.springframework.stereotype.Component;

/**
 * MustitSeller Aggregate와 MustitSellerEntity 간 변환을 담당하는 Mapper
 * <p>
 * Domain 객체와 Persistence Entity 간의 양방향 변환을 제공합니다.
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
     */
    public MustitSellerEntity toEntity(MustitSeller seller) {
        if (seller == null) {
            return null;
        }

        MustitSellerEntity.BasicInfo basicInfo = new MustitSellerEntity.BasicInfo(
                seller.getSellerId(),
                seller.getName(),
                seller.isActive()
        );

        MustitSellerEntity.CrawlInfo crawlInfo = new MustitSellerEntity.CrawlInfo(
                toEntityIntervalType(seller.getCrawlInterval().getIntervalType()),
                seller.getCrawlInterval().getIntervalValue(),
                seller.getCrawlInterval().getCronExpression()
        );

        return new MustitSellerEntity(basicInfo, crawlInfo);
    }

    /**
     * JPA Entity → Domain Aggregate 변환
     *
     * @param entity JPA Entity
     * @return Domain Aggregate
     */
    public MustitSeller toDomain(MustitSellerEntity entity) {
        if (entity == null) {
            return null;
        }

        SellerBasicInfo basicInfo = SellerBasicInfo.of(
                entity.getSellerId(),
                entity.getName(),
                entity.getIsActive()
        );

        CrawlInterval crawlInterval = new CrawlInterval(
                toDomainIntervalType(entity.getIntervalType()),
                entity.getIntervalValue()
        );

        SellerTimeInfo timeInfo = SellerTimeInfo.of(
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );

        return MustitSeller.reconstitute(basicInfo, crawlInterval, timeInfo);
    }

    /**
     * Domain CrawlIntervalType → Entity IntervalType 변환
     *
     * @param domainType Domain Enum
     * @return Entity Enum
     */
    private MustitSellerEntity.IntervalType toEntityIntervalType(CrawlIntervalType domainType) {
        if (domainType == null) {
            return null;
        }

        return switch (domainType) {
            case HOURLY -> MustitSellerEntity.IntervalType.HOURLY;
            case DAILY -> MustitSellerEntity.IntervalType.DAILY;
            case WEEKLY -> MustitSellerEntity.IntervalType.WEEKLY;
        };
    }

    /**
     * Entity IntervalType → Domain CrawlIntervalType 변환
     *
     * @param entityType Entity Enum
     * @return Domain Enum
     */
    private CrawlIntervalType toDomainIntervalType(MustitSellerEntity.IntervalType entityType) {
        if (entityType == null) {
            return null;
        }

        return switch (entityType) {
            case HOURLY -> CrawlIntervalType.HOURLY;
            case DAILY -> CrawlIntervalType.DAILY;
            case WEEKLY -> CrawlIntervalType.WEEKLY;
        };
    }
}
