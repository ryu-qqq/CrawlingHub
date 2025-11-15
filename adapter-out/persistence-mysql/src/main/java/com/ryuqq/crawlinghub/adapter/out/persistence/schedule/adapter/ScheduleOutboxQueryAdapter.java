package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto.ScheduleOutboxQueryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.ScheduleOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.ScheduleOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ScheduleOutbox Query Adapter - CQRS Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (Orchestration 패턴 지원)</li>
 *   <li>✅ QueryDSL DTO Projection으로 직접 조회 → Domain 변환</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ ScheduleOutboxQueryDslRepository 사용</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Write 작업은 ScheduleOutboxCommandAdapter에서 처리</li>
 *   <li>❌ Command 작업은 이 Adapter에서 금지</li>
 *   <li>✅ DTO → Domain 변환은 Adapter에서 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleOutboxQueryAdapter implements ScheduleOutboxQueryPort {

    private final ScheduleOutboxQueryDslRepository queryDslRepository;

    /**
     * Adapter 생성자
     *
     * @param queryDslRepository QueryDSL Repository
     */
    public ScheduleOutboxQueryAdapter(ScheduleOutboxQueryDslRepository queryDslRepository) {
        this.queryDslRepository = Objects.requireNonNull(queryDslRepository, "queryDslRepository must not be null");
    }

    /**
     * Idempotency Key로 Outbox 조회
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>ScheduleOutboxQueryDslRepository로 DTO 조회</li>
     *   <li>DTO → Domain 변환</li>
     * </ol>
     *
     * @param idemKey Idempotency Key
     * @return Outbox (Optional)
     */
    public Optional<ScheduleOutbox> findByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        return queryDslRepository.findByIdemKey(idemKey)
            .map(this::toDomain);
    }

    /**
     * Idempotency Key 존재 여부 확인
     *
     * @param idemKey Idempotency Key
     * @return 존재 여부
     */
    public boolean existsByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        return queryDslRepository.existsByIdemKey(idemKey);
    }

    /**
     * WAL State가 PENDING인 Outbox 목록 조회
     *
     * <p><strong>비즈니스 규칙:</strong> S2 Phase (Execute)에서 처리할 대상</p>
     *
     * @return PENDING 상태의 Outbox 목록
     */
    public List<ScheduleOutbox> findByWalStatePending() {
        return queryDslRepository.findByWalStatePending()
            .stream()
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
        return queryDslRepository.findByOperationStateFailed()
            .stream()
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
        return queryDslRepository.findByWalStateCompleted()
            .stream()
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

        return queryDslRepository.findByOpId(opId)
            .map(this::toDomain)
            .orElse(null);
    }

    /**
     * Seller ID로 최신 Outbox 조회
     *
     * @param sellerId Seller PK (Long FK)
     * @return Outbox (null if not found)
     */
    public ScheduleOutbox findLatestBySellerId(Long sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        return queryDslRepository.findLatestBySellerId(sellerId)
            .map(this::toDomain)
            .orElse(null);
    }

    /**
     * DTO → Domain 변환
     *
     * @param dto ScheduleOutboxQueryDto
     * @return ScheduleOutbox
     */
    private ScheduleOutbox toDomain(ScheduleOutboxQueryDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        // RetryPolicy VO 생성
        com.ryuqq.crawlinghub.domain.schedule.outbox.RetryPolicy retryPolicy =
            new com.ryuqq.crawlinghub.domain.schedule.outbox.RetryPolicy(
                dto.maxRetries(),
                dto.retryCount(),
                dto.timeoutMillis()
            );

        return ScheduleOutbox.reconstitute(
            dto.id(),
            dto.opId(),
            dto.sellerId(),
            dto.idemKey(),
            dto.eventType(),  // EventType enum (이미 올바른 타입)
            dto.payload(),
            dto.outcomeJson(),
            dto.operationState(),
            dto.walState(),
            dto.errorMessage(),
            retryPolicy,  // RetryPolicy VO
            dto.completedAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}
