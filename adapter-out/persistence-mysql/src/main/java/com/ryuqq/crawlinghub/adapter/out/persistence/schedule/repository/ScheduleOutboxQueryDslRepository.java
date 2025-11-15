package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto.ScheduleOutboxQueryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QScheduleOutboxEntity;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ScheduleOutboxQueryDslRepository - Query Repository (QueryDSL)
 *
 * <p><strong>QueryDSL 기반 읽기 전용 Repository ⭐</strong></p>
 * <ul>
 *   <li>✅ JPAQueryFactory 캡슐화</li>
 *   <li>✅ DTO Projection 최적화</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ 타입 안전한 쿼리</li>
 *   <li>✅ Orchestration Pattern 지원</li>
 * </ul>
 *
 * <p><strong>사용처:</strong></p>
 * <ul>
 *   <li>ScheduleOutboxQueryAdapter에서 주입받아 사용</li>
 *   <li>Query Adapter는 이 Repository를 통해 조회</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
@Repository
public class ScheduleOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QScheduleOutboxEntity scheduleOutboxEntity = QScheduleOutboxEntity.scheduleOutboxEntity;

    public ScheduleOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory must not be null");
    }

    /**
     * Idempotency Key로 Outbox 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회합니다.</p>
     *
     * @param idemKey Idempotency Key (null 불가)
     * @return ScheduleOutbox Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException idemKey가 null인 경우
     */
    public Optional<ScheduleOutboxQueryDto> findByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        ScheduleOutboxQueryDto result = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.payload,
                scheduleOutboxEntity.outcomeJson,
                scheduleOutboxEntity.operationState,
                scheduleOutboxEntity.walState,
                scheduleOutboxEntity.errorMessage,
                scheduleOutboxEntity.retryCount,
                scheduleOutboxEntity.maxRetries,
                scheduleOutboxEntity.timeoutMillis,
                scheduleOutboxEntity.completedAt,
                scheduleOutboxEntity.createdAt,
                scheduleOutboxEntity.updatedAt
            ))
            .from(scheduleOutboxEntity)
            .where(scheduleOutboxEntity.idemKey.eq(idemKey))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * Idempotency Key 존재 여부 확인
     *
     * @param idemKey Idempotency Key (null 불가)
     * @return 존재 여부
     * @throws IllegalArgumentException idemKey가 null인 경우
     */
    public boolean existsByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        Integer count = queryFactory
            .selectOne()
            .from(scheduleOutboxEntity)
            .where(scheduleOutboxEntity.idemKey.eq(idemKey))
            .fetchFirst();

        return count != null;
    }

    /**
     * WAL State가 PENDING인 Outbox 목록 조회
     *
     * <p><strong>비즈니스 규칙:</strong> S2 Phase (Execute)에서 처리할 대상</p>
     *
     * @return PENDING 상태의 Outbox Query DTO 목록
     */
    public List<ScheduleOutboxQueryDto> findByWalStatePending() {
        return queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.payload,
                scheduleOutboxEntity.outcomeJson,
                scheduleOutboxEntity.operationState,
                scheduleOutboxEntity.walState,
                scheduleOutboxEntity.errorMessage,
                scheduleOutboxEntity.retryCount,
                scheduleOutboxEntity.maxRetries,
                scheduleOutboxEntity.timeoutMillis,
                scheduleOutboxEntity.completedAt,
                scheduleOutboxEntity.createdAt,
                scheduleOutboxEntity.updatedAt
            ))
            .from(scheduleOutboxEntity)
            .where(scheduleOutboxEntity.walState.eq(ScheduleOutbox.WriteAheadState.PENDING))
            .orderBy(scheduleOutboxEntity.createdAt.asc())
            .fetch();
    }

    /**
     * Operation State가 FAILED인 Outbox 목록 조회
     *
     * <p><strong>비즈니스 규칙:</strong> S3 Phase (Finalize)에서 재시도 대상</p>
     *
     * @return FAILED 상태의 Outbox Query DTO 목록
     */
    public List<ScheduleOutboxQueryDto> findByOperationStateFailed() {
        return queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.payload,
                scheduleOutboxEntity.outcomeJson,
                scheduleOutboxEntity.operationState,
                scheduleOutboxEntity.walState,
                scheduleOutboxEntity.errorMessage,
                scheduleOutboxEntity.retryCount,
                scheduleOutboxEntity.maxRetries,
                scheduleOutboxEntity.timeoutMillis,
                scheduleOutboxEntity.completedAt,
                scheduleOutboxEntity.createdAt,
                scheduleOutboxEntity.updatedAt
            ))
            .from(scheduleOutboxEntity)
            .where(
                scheduleOutboxEntity.operationState.eq(ScheduleOutbox.OperationState.FAILED),
                scheduleOutboxEntity.retryCount.lt(scheduleOutboxEntity.maxRetries)
            )
            .orderBy(scheduleOutboxEntity.createdAt.asc())
            .fetch();
    }

    /**
     * WAL State가 COMPLETED인 Outbox 목록 조회
     *
     * <p><strong>비즈니스 규칙:</strong> S3 Phase (Finalize)에서 정리 대상</p>
     *
     * @return COMPLETED 상태의 Outbox Query DTO 목록
     */
    public List<ScheduleOutboxQueryDto> findByWalStateCompleted() {
        return queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.payload,
                scheduleOutboxEntity.outcomeJson,
                scheduleOutboxEntity.operationState,
                scheduleOutboxEntity.walState,
                scheduleOutboxEntity.errorMessage,
                scheduleOutboxEntity.retryCount,
                scheduleOutboxEntity.maxRetries,
                scheduleOutboxEntity.timeoutMillis,
                scheduleOutboxEntity.completedAt,
                scheduleOutboxEntity.createdAt,
                scheduleOutboxEntity.updatedAt
            ))
            .from(scheduleOutboxEntity)
            .where(scheduleOutboxEntity.walState.eq(ScheduleOutbox.WriteAheadState.COMPLETED))
            .orderBy(scheduleOutboxEntity.createdAt.asc())
            .fetch();
    }

    /**
     * OpId로 Outbox 조회
     *
     * @param opId Orchestrator OpId (UUID String, null 불가)
     * @return ScheduleOutbox Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException opId가 null인 경우
     */
    public Optional<ScheduleOutboxQueryDto> findByOpId(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        ScheduleOutboxQueryDto result = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.payload,
                scheduleOutboxEntity.outcomeJson,
                scheduleOutboxEntity.operationState,
                scheduleOutboxEntity.walState,
                scheduleOutboxEntity.errorMessage,
                scheduleOutboxEntity.retryCount,
                scheduleOutboxEntity.maxRetries,
                scheduleOutboxEntity.timeoutMillis,
                scheduleOutboxEntity.completedAt,
                scheduleOutboxEntity.createdAt,
                scheduleOutboxEntity.updatedAt
            ))
            .from(scheduleOutboxEntity)
            .where(scheduleOutboxEntity.opId.eq(opId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * Seller ID로 최신 Outbox 조회
     *
     * @param sellerId Seller PK (Long FK, null 불가)
     * @return ScheduleOutbox Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException sellerId가 null인 경우
     */
    public Optional<ScheduleOutboxQueryDto> findLatestBySellerId(Long sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        ScheduleOutboxQueryDto result = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.payload,
                scheduleOutboxEntity.outcomeJson,
                scheduleOutboxEntity.operationState,
                scheduleOutboxEntity.walState,
                scheduleOutboxEntity.errorMessage,
                scheduleOutboxEntity.retryCount,
                scheduleOutboxEntity.maxRetries,
                scheduleOutboxEntity.timeoutMillis,
                scheduleOutboxEntity.completedAt,
                scheduleOutboxEntity.createdAt,
                scheduleOutboxEntity.updatedAt
            ))
            .from(scheduleOutboxEntity)
            .where(scheduleOutboxEntity.sellerId.eq(sellerId))
            .orderBy(scheduleOutboxEntity.createdAt.desc())
            .fetchFirst();

        return Optional.ofNullable(result);
    }
}
