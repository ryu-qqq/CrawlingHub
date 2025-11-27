package com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskOutboxJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 CrawlTaskOutbox 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>CrawlTaskOutbox → CrawlTaskOutboxJpaEntity (저장용)
 *   <li>CrawlTaskOutboxJpaEntity → CrawlTaskOutbox (조회용)
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
public class CrawlTaskOutboxJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 Outbox 저장
     *   <li>기존 Outbox 상태 업데이트
     * </ul>
     *
     * @param domain CrawlTaskOutbox 도메인
     * @return CrawlTaskOutboxJpaEntity
     */
    public CrawlTaskOutboxJpaEntity toEntity(CrawlTaskOutbox domain) {
        return CrawlTaskOutboxJpaEntity.of(
                domain.getCrawlTaskId().value(),
                domain.getIdempotencyKey(),
                domain.getPayload(),
                domain.getStatus(),
                domain.getRetryCount(),
                domain.getCreatedAt(),
                domain.getProcessedAt());
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
     * @param entity CrawlTaskOutboxJpaEntity
     * @return CrawlTaskOutbox 도메인
     */
    public CrawlTaskOutbox toDomain(CrawlTaskOutboxJpaEntity entity) {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(entity.getCrawlTaskId()),
                entity.getIdempotencyKey(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getCreatedAt(),
                entity.getProcessedAt());
    }
}
