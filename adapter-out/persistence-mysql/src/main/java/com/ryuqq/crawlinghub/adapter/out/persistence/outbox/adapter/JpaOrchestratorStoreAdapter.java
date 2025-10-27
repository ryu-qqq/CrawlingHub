package com.ryuqq.crawlinghub.adapter.out.persistence.outbox.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity.OperationState;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity.WriteAheadState;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.repository.SellerCrawlScheduleOutboxJpaRepository;
import com.ryuqq.orchestrator.core.contract.Command;
import com.ryuqq.orchestrator.core.contract.Envelope;
import com.ryuqq.orchestrator.core.model.Domain;
import com.ryuqq.orchestrator.core.model.EventType;
import com.ryuqq.orchestrator.core.model.OpId;
import com.ryuqq.orchestrator.core.model.Payload;
import com.ryuqq.orchestrator.core.outcome.Outcome;
import com.ryuqq.orchestrator.core.spi.Store;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orchestrator Store SPI의 JPA 구현체
 * <p>
 * Orchestrator SDK의 {@link Store} 인터페이스를 구현하여
 * SellerCrawlScheduleOutbox 테이블에 Write-Ahead Log (WAL)를 기록합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>writeAhead(): WAL 기록 (REQUIRES_NEW 트랜잭션)</li>
 *   <li>finalize(): 작업 완료 처리 (REQUIRES_NEW 트랜잭션)</li>
 *   <li>scanWA(): WAL PENDING 건 조회 (Finalizer용)</li>
 *   <li>scanInProgress(): IN_PROGRESS 타임아웃 건 조회 (Reaper용)</li>
 *   <li>getEnvelope(): OpId로 Envelope 재구성</li>
 *   <li>getState(): OpId로 OperationState 조회</li>
 * </ul>
 * </p>
 * <p>
 * 트랜잭션 전략:
 * <ul>
 *   <li>REQUIRES_NEW: 각 메서드가 독립적인 트랜잭션을 시작하여 커밋</li>
 *   <li>이유: Outbox 작업은 Domain 트랜잭션과 독립적으로 커밋되어야 함</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class JpaOrchestratorStoreAdapter implements Store {

    private final SellerCrawlScheduleOutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * 생성자
     *
     * @param repository   Outbox JPA Repository
     * @param objectMapper JSON 직렬화/역직렬화용
     */
    public JpaOrchestratorStoreAdapter(
            SellerCrawlScheduleOutboxJpaRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Write-Ahead Log 기록 (S2: Execute 단계)
     * <p>
     * Executor 실행 전에 호출되어 Outcome을 미리 기록합니다.
     * 장애 시 Finalizer가 이를 읽어서 복구합니다.
     * </p>
     * <p>
     * 트랜잭션: REQUIRES_NEW (독립적 커밋)
     * </p>
     *
     * @param opId    Orchestrator OpId
     * @param outcome Executor 실행 결과 (Ok, Retry, Fail)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAhead(OpId opId, Outcome outcome) {
        SellerCrawlScheduleOutboxEntity outbox = repository.findByOpId(opId.getValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId.getValue()
                ));

        String outcomeJson = serializeOutcome(outcome);
        outbox.writeAhead(outcomeJson);
        repository.save(outbox);
    }

    /**
     * 작업 완료 처리 (S3: Finalize 단계)
     * <p>
     * Executor 실행 완료 후 호출되어 최종 상태로 전환합니다.
     * WAL 상태를 COMPLETED로 변경하고, OperationState를 COMPLETED/FAILED로 변경합니다.
     * </p>
     * <p>
     * 트랜잭션: REQUIRES_NEW (독립적 커밋)
     * </p>
     *
     * @param opId              Orchestrator OpId
     * @param finalState        최종 상태 (COMPLETED/FAILED)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalize(OpId opId, com.ryuqq.orchestrator.core.statemachine.OperationState finalState) {
        SellerCrawlScheduleOutboxEntity outbox = repository.findByOpId(opId.getValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId.getValue()
                ));

        OperationState entityState = mapToEntityOperationState(finalState);
        outbox.finalize(entityState);
        repository.save(outbox);
    }

    /**
     * Write-Ahead Outcome 조회
     * <p>
     * Orchestrator가 WAL에 기록된 Outcome을 조회할 때 사용합니다.
     * </p>
     *
     * @param opId Orchestrator OpId
     * @return Write-Ahead Log에 기록된 Outcome
     */
    @Override
    @Transactional(readOnly = true)
    public Outcome getWriteAheadOutcome(OpId opId) {
        SellerCrawlScheduleOutboxEntity outbox = repository.findByOpId(opId.getValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId.getValue()
                ));

        if (outbox.getOutcomeJson() == null) {
            throw new IllegalStateException(
                    "No write-ahead outcome found for OpId: " + opId.getValue()
            );
        }

        return deserializeOutcome(outbox.getOutcomeJson());
    }

    /**
     * WAL PENDING 건 조회 (Finalizer용)
     * <p>
     * Finalizer가 주기적으로 호출하여 WAL 기록 후 완료 처리가 안 된 건들을 찾습니다.
     * </p>
     *
     * @param state WAL 상태 (PENDING)
     * @param limit 조회 제한 (배치 크기)
     * @return OpId 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<OpId> scanWA(com.ryuqq.orchestrator.core.spi.WriteAheadState state, int limit) {
        WriteAheadState entityState = mapToEntityWriteAheadState(state);
        Pageable pageable = PageRequest.of(0, limit);
        List<SellerCrawlScheduleOutboxEntity> pendingOutboxes =
                repository.findByWalStatePending(entityState, pageable);

        return pendingOutboxes.stream()
                .map(outbox -> OpId.of(outbox.getOpId()))
                .toList();
    }

    /**
     * IN_PROGRESS 타임아웃 건 조회 (Reaper용)
     * <p>
     * Reaper가 주기적으로 호출하여 IN_PROGRESS 상태이지만 타임아웃된 건들을 찾습니다.
     * </p>
     *
     * @param timeoutMillis 타임아웃 (밀리초)
     * @param limit         조회 제한 (배치 크기)
     * @return OpId 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<OpId> scanInProgress(long timeoutMillis, int limit) {
        LocalDateTime cutoffTime = LocalDateTime.now()
                .minusNanos(timeoutMillis * 1_000_000);

        Pageable pageable = PageRequest.of(0, limit);
        List<SellerCrawlScheduleOutboxEntity> timedOutOutboxes =
                repository.findInProgressAndTimeout(
                        OperationState.IN_PROGRESS,
                        cutoffTime,
                        pageable
                );

        return timedOutOutboxes.stream()
                .map(outbox -> OpId.of(outbox.getOpId()))
                .toList();
    }

    /**
     * OpId로 Envelope 재구성
     * <p>
     * Finalizer/Reaper가 Envelope를 재구성하여 Executor를 다시 호출할 때 사용합니다.
     * </p>
     *
     * @param opId Orchestrator OpId
     * @return Envelope (Command + Outcome)
     */
    @Override
    @Transactional(readOnly = true)
    public Envelope getEnvelope(OpId opId) {
        SellerCrawlScheduleOutboxEntity outbox = repository.findByOpId(opId.getValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId.getValue()
                ));

        Command command = reconstructCommand(outbox);
        long acceptedAt = outbox.getCreatedAt()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return Envelope.of(opId, command, acceptedAt);
    }

    /**
     * OpId로 OperationState 조회
     * <p>
     * Orchestrator가 현재 작업의 상태를 확인할 때 사용합니다.
     * </p>
     *
     * @param opId Orchestrator OpId
     * @return OperationState (PENDING, IN_PROGRESS, COMPLETED, FAILED)
     */
    @Override
    @Transactional(readOnly = true)
    public com.ryuqq.orchestrator.core.statemachine.OperationState getState(OpId opId) {
        SellerCrawlScheduleOutboxEntity outbox = repository.findByOpId(opId.getValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox not found for OpId: " + opId.getValue()
                ));

        return mapToOrchestratorOperationState(outbox.getOperationState());
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Command 재구성 (Outbox Entity → Orchestrator Command)
     *
     * @param outbox Outbox Entity
     * @return Orchestrator Command
     */
    private Command reconstructCommand(SellerCrawlScheduleOutboxEntity outbox) {
        return Command.of(
                Domain.of(outbox.getDomain()),
                EventType.of(outbox.getEventType()),
                com.ryuqq.orchestrator.core.model.BizKey.of(outbox.getBizKey()),
                com.ryuqq.orchestrator.core.model.IdemKey.of(outbox.getIdemKey()),
                Payload.of(outbox.getPayload())
        );
    }

    /**
     * Outcome 직렬화 (Orchestrator Outcome → JSON String)
     *
     * @param outcome Orchestrator Outcome
     * @return JSON String
     */
    private String serializeOutcome(Outcome outcome) {
        try {
            return objectMapper.writeValueAsString(outcome);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Outcome", e);
        }
    }

    /**
     * Outcome 역직렬화 (JSON String → Orchestrator Outcome)
     *
     * @param outcomeJson JSON String
     * @return Orchestrator Outcome
     */
    private Outcome deserializeOutcome(String outcomeJson) {
        try {
            return objectMapper.readValue(outcomeJson, Outcome.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize Outcome", e);
        }
    }

    /**
     * Entity OperationState 변환 (Orchestrator → Entity)
     *
     * @param orchestratorState Orchestrator OperationState
     * @return Entity OperationState
     */
    private OperationState mapToEntityOperationState(
            com.ryuqq.orchestrator.core.statemachine.OperationState orchestratorState
    ) {
        return switch (orchestratorState) {
            case PENDING -> OperationState.PENDING;
            case IN_PROGRESS -> OperationState.IN_PROGRESS;
            case COMPLETED -> OperationState.COMPLETED;
            case FAILED -> OperationState.FAILED;
        };
    }

    /**
     * Orchestrator OperationState 변환 (Entity → Orchestrator)
     *
     * @param entityState Entity OperationState
     * @return Orchestrator OperationState
     */
    private com.ryuqq.orchestrator.core.statemachine.OperationState mapToOrchestratorOperationState(
            OperationState entityState
    ) {
        return switch (entityState) {
            case PENDING -> com.ryuqq.orchestrator.core.statemachine.OperationState.PENDING;
            case IN_PROGRESS -> com.ryuqq.orchestrator.core.statemachine.OperationState.IN_PROGRESS;
            case COMPLETED -> com.ryuqq.orchestrator.core.statemachine.OperationState.COMPLETED;
            case FAILED -> com.ryuqq.orchestrator.core.statemachine.OperationState.FAILED;
        };
    }

    /**
     * Entity WriteAheadState 변환 (Orchestrator → Entity)
     *
     * @param orchestratorState Orchestrator WriteAheadState
     * @return Entity WriteAheadState
     */
    private WriteAheadState mapToEntityWriteAheadState(
            com.ryuqq.orchestrator.core.spi.WriteAheadState orchestratorState
    ) {
        return switch (orchestratorState) {
            case PENDING -> WriteAheadState.PENDING;
            case COMPLETED -> WriteAheadState.COMPLETED;
        };
    }
}
