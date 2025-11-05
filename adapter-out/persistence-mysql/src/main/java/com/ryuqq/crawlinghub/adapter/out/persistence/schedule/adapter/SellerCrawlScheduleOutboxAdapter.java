package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.SellerCrawlScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.SellerCrawlScheduleOutboxEntity.OperationState;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.SellerCrawlScheduleOutboxEntity.WriteAheadState;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.SellerCrawlScheduleOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.SellerCrawlScheduleOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;
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
     * @return Outbox 도메인 모델 (Optional)
     */
    @Override
    public java.util.Optional<SellerCrawlScheduleOutbox> findByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");

        return repository.findByIdemKey(idemKey)
                .map(mapper::toDomain);
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
    public void updateOpId(Long outboxId, String opId) {
        Objects.requireNonNull(outboxId, "outboxId must not be null");
        Objects.requireNonNull(opId, "opId must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findById(outboxId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for ID: " + outboxId
                ));

        // Entity 필드 직접 업데이트 (JPA 변경 감지)
        entity = new SellerCrawlScheduleOutboxEntity(
                entity.getId(),
                opId,
                entity.getSellerId(),
                entity.getIdemKey(),
                entity.getDomain(),
                entity.getEventType(),
                entity.getBizKey(),
                entity.getPayload(),
                entity.getOutcomeJson(),
                entity.getOperationState(),
                entity.getWalState(),
                entity.getErrorMessage(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getTimeoutMillis(),
                entity.getCompletedAt()
        );
        repository.save(entity);
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
    public void markInProgress(String opId, String outcomeJson) {
        Objects.requireNonNull(opId, "opId must not be null");
        Objects.requireNonNull(outcomeJson, "outcomeJson must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findByOpId(opId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));

        // Entity 필드 직접 업데이트 (JPA 변경 감지)
        entity = new SellerCrawlScheduleOutboxEntity(
                entity.getId(),
                entity.getOpId(),
                entity.getSellerId(),
                entity.getIdemKey(),
                entity.getDomain(),
                entity.getEventType(),
                entity.getBizKey(),
                entity.getPayload(),
                outcomeJson,
                OperationState.IN_PROGRESS,
                WriteAheadState.PENDING,
                entity.getErrorMessage(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getTimeoutMillis(),
                entity.getCompletedAt()
        );
        repository.save(entity);
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
    public void markCompleted(String opId, String finalState) {
        Objects.requireNonNull(opId, "opId must not be null");
        Objects.requireNonNull(finalState, "finalState must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findByOpId(opId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));

        SellerCrawlScheduleOutboxEntity.OperationState entityState =
                SellerCrawlScheduleOutboxEntity.OperationState.valueOf(finalState);
        // Entity 필드 직접 업데이트 (JPA 변경 감지)
        entity = new SellerCrawlScheduleOutboxEntity(
                entity.getId(),
                entity.getOpId(),
                entity.getSellerId(),
                entity.getIdemKey(),
                entity.getDomain(),
                entity.getEventType(),
                entity.getBizKey(),
                entity.getPayload(),
                entity.getOutcomeJson(),
                entityState,
                WriteAheadState.COMPLETED,
                entity.getErrorMessage(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getTimeoutMillis(),
                LocalDateTime.now()
        );
        repository.save(entity);
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
    public String getOperationState(String opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        SellerCrawlScheduleOutboxEntity entity = repository.findByOpId(opId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId
                ));

        return entity.getOperationState().name();
    }

    /**
     * Idempotency Key 존재 여부 확인
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param idemKey Idempotency Key
     * @return 존재 여부
     */
    @Override
    public boolean existsByIdemKey(String idemKey) {
        Objects.requireNonNull(idemKey, "idemKey must not be null");
        return repository.findByIdemKey(idemKey).isPresent();
    }

    /**
     * WAL State가 PENDING인 Outbox 목록 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @return PENDING 상태의 Outbox 목록
     */
    @Override
    public List<SellerCrawlScheduleOutbox> findByWalStatePending() {
        List<SellerCrawlScheduleOutboxEntity> entities =
                repository.findByWalStateOrderByCreatedAtAsc(
                        SellerCrawlScheduleOutboxEntity.WriteAheadState.PENDING,
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
                );
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Operation State가 FAILED인 Outbox 목록 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @return FAILED 상태의 Outbox 목록
     */
    @Override
    public List<SellerCrawlScheduleOutbox> findByOperationStateFailed() {
        List<SellerCrawlScheduleOutboxEntity> entities =
                repository.findRetryableFailed(
                        SellerCrawlScheduleOutboxEntity.OperationState.FAILED,
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
                );
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * WAL State가 COMPLETED인 Outbox 목록 조회
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @return COMPLETED 상태의 Outbox 목록
     */
    @Override
    public List<SellerCrawlScheduleOutbox> findByWalStateCompleted() {
        List<SellerCrawlScheduleOutboxEntity> entities =
                repository.findByWalStateOrderByCreatedAtAsc(
                        SellerCrawlScheduleOutboxEntity.WriteAheadState.COMPLETED,
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
                );
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Outbox 삭제
     * <p>
     * 트랜잭션은 Application Layer에서 관리됩니다.
     * </p>
     *
     * @param outbox 삭제할 Outbox
     */
    @Override
    public void delete(SellerCrawlScheduleOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");
        if (outbox.getId() == null) {
            throw new IllegalArgumentException("Outbox ID가 없어 삭제할 수 없습니다");
        }
        repository.deleteById(outbox.getId());
    }
}
