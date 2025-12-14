package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerOutBoxJpaEntityMapper - OutBox Entity ↔ Domain 변환 Mapper
 *
 * <p>CrawlSchedulerOutBox Domain과 JPA Entity 간 변환을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain CrawlSchedulerOutBox 도메인
     * @return CrawlSchedulerOutBoxJpaEntity
     */
    public CrawlSchedulerOutBoxJpaEntity toEntity(CrawlSchedulerOutBox domain) {
        return CrawlSchedulerOutBoxJpaEntity.of(
                domain.getOutBoxIdValue(),
                domain.getHistoryIdValue(),
                domain.getStatus(),
                domain.getEventPayload(),
                domain.getErrorMessage(),
                domain.getVersion(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getProcessedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity CrawlSchedulerOutBoxJpaEntity
     * @return CrawlSchedulerOutBox 도메인
     */
    public CrawlSchedulerOutBox toDomain(CrawlSchedulerOutBoxJpaEntity entity) {
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(entity.getId()),
                CrawlSchedulerHistoryId.of(entity.getHistoryId()),
                entity.getStatus(),
                entity.getEventPayload(),
                entity.getErrorMessage(),
                entity.getVersion(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getProcessedAt()));
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
