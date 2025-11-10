package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto.ScheduleOutboxQueryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntity;
import com.ryuqq.crawlinghub.application.schedule.port.out.ScheduleOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QScheduleOutboxEntity.scheduleOutboxEntity;

/**
 * ScheduleOutbox Query Adapter (CQRS - Query, QueryDSL)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ R (Read) 작업 전담</li>
 *   <li>✅ QueryDSL Projections.constructor() 사용</li>
 *   <li>✅ DTO 직접 반환 후 Domain 변환</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>✅ Query (읽기) 전용 Adapter</li>
 *   <li>✅ Command (쓰기)는 ScheduleOutboxCommandAdapter에 위임</li>
 *   <li>✅ JPAQueryFactory 사용</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Constructor</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ Objects.requireNonNull() 검증</li>
 *   <li>✅ QueryDSL Projections.constructor() 사용</li>
 *   <li>✅ DTO Record 패턴</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleOutboxQueryAdapter implements ScheduleOutboxQueryPort {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자
     *
     * @param queryFactory JPAQueryFactory
     */
    public ScheduleOutboxQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory must not be null");
    }

    /**
     * Idempotency Key로 Outbox 조회
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>QueryDSL로 DTO 조회</li>
     *   <li>DTO → Domain 변환</li>
     * </ol>
     *
     * @param idemKey Idempotency Key
     * @return Outbox (Optional)
     */
    public Optional<ScheduleOutbox> findByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        ScheduleOutboxQueryDto dto = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.domain,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.bizKey,
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

        return Optional.ofNullable(dto).map(this::toDomain);
    }

    /**
     * Idempotency Key 존재 여부 확인
     *
     * @param idemKey Idempotency Key
     * @return 존재 여부
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
     * @return PENDING 상태의 Outbox 목록
     */
    public List<ScheduleOutbox> findByWalStatePending() {
        List<ScheduleOutboxQueryDto> dtos = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.domain,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.bizKey,
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

        return dtos.stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * Operation State가 FAILED인 Outbox 목록 조회
     *
     * <p><strong>비즈니스 규칙:</strong> S3 Phase (Finalize)에서 재시도 대상</p>
     *
     * @return FAILED 상태의 Outbox 목록
     */
    public List<ScheduleOutbox> findByOperationStateFailed() {
        List<ScheduleOutboxQueryDto> dtos = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.domain,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.bizKey,
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

        return dtos.stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * WAL State가 COMPLETED인 Outbox 목록 조회
     *
     * <p><strong>비즈니스 규칙:</strong> S3 Phase (Finalize)에서 정리 대상</p>
     *
     * @return COMPLETED 상태의 Outbox 목록
     */
    public List<ScheduleOutbox> findByWalStateCompleted() {
        List<ScheduleOutboxQueryDto> dtos = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.domain,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.bizKey,
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

        return dtos.stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * OpId로 Outbox 조회
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outbox (null if not found)
     */
    public ScheduleOutbox findByOpId(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        ScheduleOutboxQueryDto dto = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.domain,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.bizKey,
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

        return dto != null ? toDomain(dto) : null;
    }

    /**
     * Seller ID로 최신 Outbox 조회
     *
     * @param sellerId Seller PK (Long FK)
     * @return Outbox (null if not found)
     */
    public ScheduleOutbox findLatestBySellerId(Long sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        ScheduleOutboxQueryDto dto = queryFactory
            .select(Projections.constructor(
                ScheduleOutboxQueryDto.class,
                scheduleOutboxEntity.id,
                scheduleOutboxEntity.opId,
                scheduleOutboxEntity.sellerId,
                scheduleOutboxEntity.idemKey,
                scheduleOutboxEntity.domain,
                scheduleOutboxEntity.eventType,
                scheduleOutboxEntity.bizKey,
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

        return dto != null ? toDomain(dto) : null;
    }

    /**
     * DTO → Domain 변환
     *
     * @param dto ScheduleOutboxQueryDto
     * @return ScheduleOutbox
     */
    private ScheduleOutbox toDomain(ScheduleOutboxQueryDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        return ScheduleOutbox.reconstitute(
            dto.id(),
            dto.opId(),
            dto.sellerId(),
            dto.idemKey(),
            dto.domain(),
            dto.eventType(),
            dto.bizKey(),
            dto.payload(),
            dto.outcomeJson(),
            dto.operationState(),
            dto.walState(),
            dto.errorMessage(),
            dto.retryCount(),
            dto.maxRetries(),
            dto.timeoutMillis(),
            dto.completedAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}
