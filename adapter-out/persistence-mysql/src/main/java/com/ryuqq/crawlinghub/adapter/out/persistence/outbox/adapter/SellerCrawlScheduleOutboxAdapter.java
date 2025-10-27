package com.ryuqq.crawlinghub.adapter.out.persistence.outbox.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.mapper.SellerCrawlScheduleOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.repository.SellerCrawlScheduleOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.outbox.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.outbox.SellerCrawlScheduleOutbox;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 셀러 크롤링 스케줄 Outbox Adapter (Persistence Adapter)
 * <p>
 * SellerCrawlScheduleOutboxPort의 JPA 구현체입니다.
 * Domain Model과 JPA Entity 간의 변환을 담당합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>Domain → Entity 변환</li>
 *   <li>Entity → Domain 변환</li>
 *   <li>JPA Repository 호출</li>
 *   <li>트랜잭션 관리</li>
 * </ul>
 * </p>
 * <p>
 * 트랜잭션 전략:
 * <ul>
 *   <li>REQUIRES_NEW: 독립적인 Outbox 트랜잭션</li>
 *   <li>이유: Domain 트랜잭션과 분리하여 Outbox만 커밋</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class SellerCrawlScheduleOutboxAdapter implements SellerCrawlScheduleOutboxPort {

    private final SellerCrawlScheduleOutboxJpaRepository repository;
    private final SellerCrawlScheduleOutboxMapper mapper;

    /**
     * 생성자
     *
     * @param repository Outbox JPA Repository
     * @param mapper     Domain ↔ Entity Mapper
     */
    public SellerCrawlScheduleOutboxAdapter(
            SellerCrawlScheduleOutboxJpaRepository repository,
            SellerCrawlScheduleOutboxMapper mapper
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    /**
     * Outbox 저장
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param outbox Outbox 도메인 모델
     * @return 저장된 Outbox (ID 포함)
     */
    @Override
    public SellerCrawlScheduleOutbox save(SellerCrawlScheduleOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");

        SellerCrawlScheduleOutboxEntity entity = mapper.toEntity(outbox);
        SellerCrawlScheduleOutboxEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * OpId로 Outbox 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    @Override
    public SellerCrawlScheduleOutbox findByOpId(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        return repository.findByOpId(opId)
                .map(mapper::toDomain)
                .orElse(null);
    }

    /**
     * Idempotency Key로 Outbox 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param idemKey Idempotency Key
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    @Override
    public SellerCrawlScheduleOutbox findByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        return repository.findByIdemKey(idemKey)
                .map(mapper::toDomain)
                .orElse(null);
    }

    /**
     * Seller ID로 최신 Outbox 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param sellerId Seller PK (Long FK)
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    @Override
    public SellerCrawlScheduleOutbox findLatestBySellerId(Long sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        List<SellerCrawlScheduleOutboxEntity> outboxes =
                repository.findBySellerIdOrderByCreatedAtDesc(sellerId);

        if (outboxes.isEmpty()) {
            return null;
        }

        return mapper.toDomain(outboxes.get(0));
    }

    /**
     * OpId 업데이트
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * 불변 Entity를 위해 static factory method를 사용합니다.
     * </p>
     *
     * @param outboxId Outbox PK
     * @param opId     Orchestrator OpId
     */
    @Override
    public void updateOpId(Long outboxId, String opId) {
        Objects.requireNonNull(outboxId, "outboxId must not be null");
        Objects.requireNonNull(opId, "opId must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findById(outboxId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for ID: " + outboxId
                ));

        SellerCrawlScheduleOutboxEntity updatedEntity =
                SellerCrawlScheduleOutboxEntity.withOpId(entity, opId);
        repository.save(updatedEntity);
    }

    // ========================================
    // Orchestrator Store 통합 메서드 구현
    // ========================================

    /**
     * 작업 진행 중 상태로 표시
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * 불변 Entity를 위해 static factory method를 사용합니다.
     * </p>
     *
     * @param opId        Orchestrator OpId (UUID String)
     * @param outcomeJson 실행 결과 JSON
     */
    @Override
    public void markInProgress(String opId, String outcomeJson) {
        Objects.requireNonNull(opId, "opId must not be null");
        Objects.requireNonNull(outcomeJson, "outcomeJson must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findByOpId(opId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));

        SellerCrawlScheduleOutboxEntity updatedEntity =
                SellerCrawlScheduleOutboxEntity.withWriteAhead(entity, outcomeJson);
        repository.save(updatedEntity);
    }

    /**
     * 작업 완료 상태로 표시
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * 불변 Entity를 위해 static factory method를 사용합니다.
     * </p>
     *
     * @param opId       Orchestrator OpId (UUID String)
     * @param finalState 최종 상태 (COMPLETED 또는 FAILED)
     */
    @Override
    public void markCompleted(String opId, String finalState) {
        Objects.requireNonNull(opId, "opId must not be null");
        Objects.requireNonNull(finalState, "finalState must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findByOpId(opId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));

        SellerCrawlScheduleOutboxEntity.OperationState entityState =
                SellerCrawlScheduleOutboxEntity.OperationState.valueOf(finalState);
        SellerCrawlScheduleOutboxEntity updatedEntity =
                SellerCrawlScheduleOutboxEntity.withFinalized(entity, entityState);
        repository.save(updatedEntity);
    }

    /**
     * 진행 중 작업의 결과 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outcome JSON (존재하지 않으면 null)
     */
    @Override
    public String getInProgressOutcome(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findByOpId(opId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));

        return entity.getOutcomeJson();
    }

    /**
     * 대기 중인 작업 목록 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param limit 조회 제한 (배치 크기)
     * @return OpId 목록 (UUID String)
     */
    @Override
    public List<String> findPendingOperations(int limit) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, limit);

        List<SellerCrawlScheduleOutboxEntity> pendingOutboxes =
                repository.findByWalStateOrderByCreatedAtAsc(
                        SellerCrawlScheduleOutboxEntity.WriteAheadState.PENDING,
                        pageable
                );

        return pendingOutboxes.stream()
                .map(SellerCrawlScheduleOutboxEntity::getOpId)
                .toList();
    }

    /**
     * 타임아웃된 작업 목록 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param timeoutMillis 타임아웃 시간 (밀리초)
     * @param limit         조회 제한 (배치 크기)
     * @return OpId 목록 (UUID String)
     */
    @Override
    public List<String> findTimeoutOperations(long timeoutMillis, int limit) {
        LocalDateTime cutoffTime = LocalDateTime.now()
                .minusNanos(timeoutMillis * 1_000_000);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, limit);

        List<SellerCrawlScheduleOutboxEntity> timedOutOutboxes =
                repository.findInProgressAndTimeout(
                        SellerCrawlScheduleOutboxEntity.OperationState.IN_PROGRESS,
                        cutoffTime,
                        pageable
                );

        return timedOutOutboxes.stream()
                .map(SellerCrawlScheduleOutboxEntity::getOpId)
                .toList();
    }

    /**
     * 작업 Command 정보 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outbox 도메인 모델 (Command 재구성용)
     */
    @Override
    public SellerCrawlScheduleOutbox getOperationEnvelope(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        return repository.findByOpId(opId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));
    }

    /**
     * 작업 상태 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return 작업 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)
     */
    @Override
    public String getOperationState(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findByOpId(opId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));

        return entity.getOperationState().name();
    }
}
