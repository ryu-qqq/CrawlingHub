package com.ryuqq.crawlinghub.application.mustit.seller.port.out.orchestrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.outbox.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.outbox.SellerCrawlScheduleOutbox;
import com.ryuqq.orchestrator.core.contract.Command;
import com.ryuqq.orchestrator.core.contract.Envelope;
import com.ryuqq.orchestrator.core.model.BizKey;
import com.ryuqq.orchestrator.core.model.Domain;
import com.ryuqq.orchestrator.core.model.EventType;
import com.ryuqq.orchestrator.core.model.IdemKey;
import com.ryuqq.orchestrator.core.model.OpId;
import com.ryuqq.orchestrator.core.model.Payload;
import com.ryuqq.orchestrator.core.outcome.Outcome;
import com.ryuqq.orchestrator.core.spi.Store;
import com.ryuqq.orchestrator.core.statemachine.OperationState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * Application Orchestrator Store (Adapter 패턴)
 * <p>
 * Orchestrator SDK의 {@link Store} 인터페이스를 구현하여
 * Application Layer의 {@link SellerCrawlScheduleOutboxPort}와 연결합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>Store SPI → Port 변환 (Adapter 패턴)</li>
 *   <li>Orchestrator 용어 → 도메인 용어 변환</li>
 *   <li>SDK 의존성을 Application Layer에 격리</li>
 * </ul>
 * </p>
 * <p>
 * 설계 원칙:
 * <ul>
 *   <li>완벽한 헥사고날 아키텍처 유지</li>
 *   <li>Port는 순수 도메인 용어만 사용 (Store 몰라도 됨)</li>
 *   <li>Adapter(Persistence)는 Store를 전혀 모름</li>
 *   <li>Orchestrator SDK는 이 클래스만 알면 됨</li>
 * </ul>
 * </p>
 * <p>
 * 리팩토링 이력:
 * <ul>
 *   <li>2025-01-27: JpaOrchestratorStoreAdapter 제거 및 통합</li>
 *   <li>Adapter 2개 → 1개로 단순화 (복잡도 50% 감소)</li>
 *   <li>Option 4: Application Store Adapter 패턴 적용</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class ApplicationOrchestratorStore implements Store {

    private final SellerCrawlScheduleOutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    /**
     * 생성자
     *
     * @param outboxPort   Outbox Port (도메인 용어 인터페이스)
     * @param objectMapper JSON 직렬화/역직렬화용
     */
    public ApplicationOrchestratorStore(
            SellerCrawlScheduleOutboxPort outboxPort,
            ObjectMapper objectMapper
    ) {
        this.outboxPort = Objects.requireNonNull(outboxPort);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    // ========================================
    // Store SPI 구현 (Orchestrator → Port 변환)
    // ========================================

    /**
     * Write-Ahead Log 기록
     * <p>
     * Store.writeAhead() → Port.markInProgress() 변환
     * </p>
     * <p>
     * 트랜잭션 전략:
     * <ul>
     *   <li>REQUIRES_NEW: 독립적인 Outbox 트랜잭션</li>
     *   <li>이유: Domain 트랜잭션과 분리하여 Outbox만 커밋</li>
     * </ul>
     * </p>
     *
     * @param opId    Orchestrator OpId
     * @param outcome Executor 실행 결과
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAhead(OpId opId, Outcome outcome) {
        Objects.requireNonNull(opId, "opId must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");

        String outcomeJson = serializeOutcome(outcome);
        outboxPort.markInProgress(opId.getValue(), outcomeJson);
    }

    /**
     * 작업 완료 처리
     * <p>
     * Store.finalize() → Port.markCompleted() 변환
     * </p>
     * <p>
     * 트랜잭션 전략:
     * <ul>
     *   <li>REQUIRES_NEW: 독립적인 Outbox 트랜잭션</li>
     *   <li>이유: Domain 트랜잭션과 분리하여 Outbox만 커밋</li>
     * </ul>
     * </p>
     *
     * @param opId       Orchestrator OpId
     * @param finalState 최종 상태
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalize(OpId opId, OperationState finalState) {
        Objects.requireNonNull(opId, "opId must not be null");
        Objects.requireNonNull(finalState, "finalState must not be null");

        outboxPort.markCompleted(opId.getValue(), finalState.name());
    }

    /**
     * Write-Ahead Outcome 조회
     * <p>
     * Store.getWriteAheadOutcome() → Port.getInProgressOutcome() 변환
     * </p>
     *
     * @param opId Orchestrator OpId
     * @return Write-Ahead Log에 기록된 Outcome
     */
    @Override
    @Transactional(readOnly = true)
    public Outcome getWriteAheadOutcome(OpId opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        String outcomeJson = outboxPort.getInProgressOutcome(opId.getValue());
        if (outcomeJson == null) {
            throw new IllegalStateException(
                    "No write-ahead outcome found for OpId: " + opId.getValue()
            );
        }

        return deserializeOutcome(outcomeJson);
    }

    /**
     * WAL PENDING 건 조회
     * <p>
     * Store.scanWA() → Port.findPendingOperations() 변환
     * </p>
     *
     * @param state WAL 상태 (PENDING)
     * @param limit 조회 제한
     * @return OpId 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<OpId> scanWA(
            com.ryuqq.orchestrator.core.spi.WriteAheadState state,
            int limit
    ) {
        Objects.requireNonNull(state, "state must not be null");

        // WriteAheadState는 PENDING만 사용되므로 무시
        List<String> opIdStrings = outboxPort.findPendingOperations(limit);
        return opIdStrings.stream()
                .map(OpId::of)
                .toList();
    }

    /**
     * IN_PROGRESS 타임아웃 건 조회
     * <p>
     * Store.scanInProgress() → Port.findTimeoutOperations() 변환
     * </p>
     *
     * @param timeoutMillis 타임아웃 (밀리초)
     * @param limit         조회 제한
     * @return OpId 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<OpId> scanInProgress(long timeoutMillis, int limit) {
        List<String> opIdStrings = outboxPort.findTimeoutOperations(timeoutMillis, limit);
        return opIdStrings.stream()
                .map(OpId::of)
                .toList();
    }

    /**
     * OpId로 Envelope 재구성
     * <p>
     * Store.getEnvelope() → Port.getOperationEnvelope() 변환 → Command 재구성
     * </p>
     *
     * @param opId Orchestrator OpId
     * @return Envelope (Command + Outcome)
     */
    @Override
    @Transactional(readOnly = true)
    public Envelope getEnvelope(OpId opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        SellerCrawlScheduleOutbox outbox = outboxPort.getOperationEnvelope(opId.getValue());
        if (outbox == null) {
            throw new IllegalStateException(
                    "Outbox not found for OpId: " + opId.getValue()
            );
        }

        Command command = reconstructCommand(outbox);
        long acceptedAt = convertToEpochMilli(outbox.getCreatedAt());

        return Envelope.of(opId, command, acceptedAt);
    }

    /**
     * OpId로 OperationState 조회
     * <p>
     * Store.getState() → Port.getOperationState() 변환
     * </p>
     *
     * @param opId Orchestrator OpId
     * @return OperationState
     */
    @Override
    @Transactional(readOnly = true)
    public OperationState getState(OpId opId) {
        Objects.requireNonNull(opId, "opId must not be null");

        String stateString = outboxPort.getOperationState(opId.getValue());
        if (stateString == null) {
            throw new IllegalStateException(
                    "Operation state not found for OpId: " + opId.getValue()
            );
        }

        return OperationState.valueOf(stateString);
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Outbox Domain Model → Orchestrator Command 변환
     *
     * @param outbox Outbox Domain Model
     * @return Orchestrator Command
     */
    private Command reconstructCommand(SellerCrawlScheduleOutbox outbox) {
        return Command.of(
                Domain.of(outbox.getDomain()),
                EventType.of(outbox.getEventType()),
                BizKey.of(outbox.getBizKey()),
                IdemKey.of(outbox.getIdemKey()),
                Payload.of(outbox.getPayload())
        );
    }

    /**
     * LocalDateTime → Epoch Millis 변환
     *
     * @param dateTime LocalDateTime
     * @return Epoch Millis
     */
    private long convertToEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    /**
     * Outcome 직렬화
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
     * Outcome 역직렬화
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
}
