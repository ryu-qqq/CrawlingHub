package com.ryuqq.crawlinghub.adapter.out.persistence.execution.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionResult;
import com.ryuqq.crawlinghub.domain.execution.vo.ExecutionDuration;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.springframework.stereotype.Component;

/**
 * CrawlExecutionJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 CrawlExecution 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>CrawlExecution → CrawlExecutionJpaEntity (저장용)
 *   <li>CrawlExecutionJpaEntity → CrawlExecution (조회용)
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
public class CrawlExecutionJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 CrawlExecution 저장 (ID가 null)
     *   <li>기존 CrawlExecution 수정 (ID가 있음)
     * </ul>
     *
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>ID: Domain.getId().value() → Entity.id (null이면 신규)
     *   <li>CrawlExecutionResult → responseBody, httpStatusCode, errorMessage
     *   <li>ExecutionDuration → startedAt, completedAt, durationMs
     * </ul>
     *
     * @param domain CrawlExecution 도메인
     * @return CrawlExecutionJpaEntity
     */
    public CrawlExecutionJpaEntity toEntity(CrawlExecution domain) {
        CrawlExecutionResult result = domain.getResult();
        ExecutionDuration duration = domain.getDuration();

        return CrawlExecutionJpaEntity.of(
                domain.getId().value(),
                domain.getCrawlTaskId().value(),
                domain.getCrawlSchedulerId().value(),
                domain.getSellerId().value(),
                domain.getStatus(),
                result != null ? result.responseBody() : null,
                result != null ? result.httpStatusCode() : null,
                result != null ? result.errorMessage() : null,
                duration != null ? duration.startedAt() : null,
                duration != null ? duration.completedAt() : null,
                duration != null ? duration.durationMs() : null,
                domain.getCreatedAt());
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
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>Entity.id → CrawlExecutionId
     *   <li>responseBody, httpStatusCode, errorMessage → CrawlExecutionResult
     *   <li>startedAt, completedAt, durationMs → ExecutionDuration
     * </ul>
     *
     * @param entity CrawlExecutionJpaEntity
     * @return CrawlExecution 도메인
     */
    public CrawlExecution toDomain(CrawlExecutionJpaEntity entity) {
        return CrawlExecution.reconstitute(
                CrawlExecutionId.of(entity.getId()),
                CrawlTaskId.of(entity.getCrawlTaskId()),
                CrawlSchedulerId.of(entity.getCrawlSchedulerId()),
                SellerId.of(entity.getSellerId()),
                entity.getStatus(),
                CrawlExecutionResult.of(
                        entity.getResponseBody(),
                        entity.getHttpStatusCode(),
                        entity.getErrorMessage()),
                ExecutionDuration.reconstitute(
                        entity.getStartedAt(), entity.getCompletedAt(), entity.getDurationMs()),
                entity.getCreatedAt());
    }
}
