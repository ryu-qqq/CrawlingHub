package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 Domain 객체 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>CrawlScheduler → CrawlSchedulerJpaEntity (저장용)
 *   <li>CrawlSchedulerJpaEntity → CrawlScheduler (조회용)
 *   <li>Value Object 추출 및 재구성
 * </ul>
 *
 * <p><strong>Hexagonal Architecture 관점:</strong>
 *
 * <ul>
 *   <li>Adapter Layer의 책임
 *   <li>Domain과 Infrastructure 기술 분리
 *   <li>Domain은 JPA 의존성 없음
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 CrawlScheduler 저장 (ID가 null)
     *   <li>기존 CrawlScheduler 수정 (ID가 있음)
     * </ul>
     *
     * @param domain CrawlScheduler 도메인
     * @return CrawlSchedulerJpaEntity
     */
    public CrawlSchedulerJpaEntity toEntity(CrawlScheduler domain) {
        return CrawlSchedulerJpaEntity.of(
                domain.getCrawlSchedulerIdValue(),
                domain.getSellerIdValue(),
                domain.getSchedulerNameValue(),
                domain.getCronExpressionValue(),
                domain.getStatus(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getUpdatedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>데이터베이스에서 조회한 Entity를 Domain으로 변환
     *   <li>Application Layer로 전달
     * </ul>
     *
     * @param entity CrawlSchedulerJpaEntity
     * @return CrawlScheduler 도메인
     */
    public CrawlScheduler toDomain(CrawlSchedulerJpaEntity entity) {
        return CrawlScheduler.reconstitute(
                CrawlSchedulerId.of(entity.getId()),
                SellerId.of(entity.getSellerId()),
                SchedulerName.of(entity.getSchedulerName()),
                CronExpression.of(entity.getCronExpression()),
                entity.getStatus(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getUpdatedAt()));
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
