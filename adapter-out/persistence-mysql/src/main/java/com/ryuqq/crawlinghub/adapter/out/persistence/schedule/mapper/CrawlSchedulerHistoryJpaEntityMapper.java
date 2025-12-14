package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerHistoryJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerHistoryJpaEntityMapper - History Entity ↔ Domain 변환 Mapper
 *
 * <p>CrawlSchedulerHistory Domain과 JPA Entity 간 변환을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerHistoryJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain CrawlSchedulerHistory 도메인
     * @return CrawlSchedulerHistoryJpaEntity
     */
    public CrawlSchedulerHistoryJpaEntity toEntity(CrawlSchedulerHistory domain) {
        return CrawlSchedulerHistoryJpaEntity.of(
                domain.getHistoryIdValue(),
                domain.getCrawlSchedulerIdValue(),
                domain.getSellerIdValue(),
                domain.getSchedulerNameValue(),
                domain.getCronExpressionValue(),
                domain.getStatus(),
                toLocalDateTime(domain.getCreatedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity CrawlSchedulerHistoryJpaEntity
     * @return CrawlSchedulerHistory 도메인
     */
    public CrawlSchedulerHistory toDomain(CrawlSchedulerHistoryJpaEntity entity) {
        return CrawlSchedulerHistory.reconstitute(
                CrawlSchedulerHistoryId.of(entity.getId()),
                CrawlSchedulerId.of(entity.getCrawlSchedulerId()),
                SellerId.of(entity.getSellerId()),
                SchedulerName.of(entity.getSchedulerName()),
                CronExpression.of(entity.getCronExpression()),
                entity.getStatus(),
                toInstant(entity.getCreatedAt()));
    }

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
