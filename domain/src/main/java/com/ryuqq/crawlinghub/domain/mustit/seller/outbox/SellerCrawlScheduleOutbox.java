package com.ryuqq.crawlinghub.domain.mustit.seller.outbox;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 셀러 크롤링 스케줄 Outbox (Domain Model)
 * <p>
 * Transactional Outbox Pattern의 도메인 모델입니다.
 * Orchestrator SDK와의 통합을 위한 순수 도메인 객체로,
 * 영속성 기술에 의존하지 않습니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>Orchestrator Command 정보 보관</li>
 *   <li>OpId 추적</li>
 *   <li>작업 상태 관리 (PENDING, IN_PROGRESS, COMPLETED, FAILED)</li>
 *   <li>Write-Ahead Log (WAL) 상태 관리</li>
 * </ul>
 * </p>
 * <p>
 * 생성 방법:
 * <ul>
 *   <li>새로운 Outbox: {@link #of(CommandInfo, Long, String)}</li>
 *   <li>Persistence 복원: {@link #restore(Long, String, CommandInfo, Long, String, OperationState, WriteAheadState, String, Integer, Integer, LocalDateTime, LocalDateTime)}</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class SellerCrawlScheduleOutbox {

    // ========================================
    // Fields
    // ========================================

    private Long id;
    private String opId;  // Orchestrator OpId (UUID)
    private CommandInfo commandInfo;
    private Long sellerId;
    private String payload;  // JSON String
    private OperationState operationState;
    private WriteAheadState walState;
    private String outcomeJson;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // ========================================
    // Constructors (Private)
    // ========================================

    /**
     * Private 생성자 - 새로운 Outbox (PENDING 상태)
     * <p>
     * 외부에서 직접 생성하지 못하도록 private으로 선언합니다.
     * {@link #of(CommandInfo, Long, String)} 팩토리 메서드를 사용하세요.
     * </p>
     */
    private SellerCrawlScheduleOutbox(
            CommandInfo commandInfo,
            Long sellerId,
            String payload
    ) {
        this.commandInfo = Objects.requireNonNull(commandInfo, "commandInfo must not be null");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.operationState = OperationState.PENDING;
        this.walState = WriteAheadState.PENDING;
        this.retryCount = 0;
        this.maxRetries = 3;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Private 생성자 - 전체 필드 복원 (Persistence Layer용)
     * <p>
     * {@link #restore} 팩토리 메서드를 사용하세요.
     * </p>
     */
    private SellerCrawlScheduleOutbox(
            Long id,
            String opId,
            CommandInfo commandInfo,
            Long sellerId,
            String payload,
            OperationState operationState,
            WriteAheadState walState,
            String outcomeJson,
            Integer retryCount,
            Integer maxRetries,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.opId = opId;
        this.commandInfo = commandInfo;
        this.sellerId = sellerId;
        this.payload = payload;
        this.operationState = operationState;
        this.walState = walState;
        this.outcomeJson = outcomeJson;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // ========================================
    // Static Factory Methods
    // ========================================

    /**
     * 새로운 Outbox 생성 (정적 팩토리 메서드)
     * <p>
     * Application Layer에서 새로운 Outbox를 생성할 때 사용합니다.
     * 초기 상태는 PENDING, WAL 상태는 PENDING입니다.
     * </p>
     *
     * @param commandInfo Command 정보
     * @param sellerId    셀러 PK (Long FK)
     * @param payload     Payload JSON
     * @return 새로운 SellerCrawlScheduleOutbox 인스턴스
     */
    public static SellerCrawlScheduleOutbox of(
            CommandInfo commandInfo,
            Long sellerId,
            String payload
    ) {
        return new SellerCrawlScheduleOutbox(commandInfo, sellerId, payload);
    }

    /**
     * Outbox 복원 (정적 팩토리 메서드)
     * <p>
     * Persistence Layer에서 기존 Outbox를 복원할 때 사용합니다.
     * 모든 필드를 포함한 완전한 상태로 복원됩니다.
     * </p>
     *
     * @param id             Outbox PK
     * @param opId           Orchestrator OpId
     * @param commandInfo    Command 정보
     * @param sellerId       셀러 PK
     * @param payload        Payload JSON
     * @param operationState 작업 상태
     * @param walState       WAL 상태
     * @param outcomeJson    Outcome JSON
     * @param retryCount     재시도 횟수
     * @param maxRetries     최대 재시도 횟수
     * @param createdAt      생성 시각
     * @param completedAt    완료 시각
     * @return 복원된 SellerCrawlScheduleOutbox 인스턴스
     */
    public static SellerCrawlScheduleOutbox restore(
            Long id,
            String opId,
            CommandInfo commandInfo,
            Long sellerId,
            String payload,
            OperationState operationState,
            WriteAheadState walState,
            String outcomeJson,
            Integer retryCount,
            Integer maxRetries,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        return new SellerCrawlScheduleOutbox(
                id, opId, commandInfo, sellerId, payload,
                operationState, walState, outcomeJson,
                retryCount, maxRetries, createdAt, completedAt
        );
    }

    // ========================================
    // Business Methods
    // ========================================

    /**
     * OpId 설정 (Orchestrator 시작 후)
     *
     * @param newOpId Orchestrator OpId
     */
    public void assignOpId(String newOpId) {
        Objects.requireNonNull(newOpId, "opId must not be null");
        if (this.opId != null) {
            throw new IllegalStateException("OpId already assigned: " + this.opId);
        }
        this.opId = newOpId;
    }

    /**
     * Write-Ahead Log 기록
     *
     * @param outcome Outcome JSON
     */
    public void writeAhead(String outcome) {
        Objects.requireNonNull(outcome, "outcomeJson must not be null");
        this.walState = WriteAheadState.PENDING;
        this.outcomeJson = outcome;
        this.operationState = OperationState.IN_PROGRESS;
    }

    /**
     * 작업 완료 처리
     *
     * @param finalState 최종 상태 (COMPLETED 또는 FAILED)
     */
    public void finalize(OperationState finalState) {
        Objects.requireNonNull(finalState, "finalState must not be null");
        if (finalState != OperationState.COMPLETED && finalState != OperationState.FAILED) {
            throw new IllegalArgumentException(
                    "finalState must be COMPLETED or FAILED, but was: " + finalState
            );
        }
        this.walState = WriteAheadState.COMPLETED;
        this.operationState = finalState;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 재시도 증가
     *
     * @return 재시도 가능 여부
     */
    public boolean incrementRetryCount() {
        this.retryCount++;
        return this.retryCount < this.maxRetries;
    }

    /**
     * 재시도 가능 여부
     *
     * @return true if 재시도 가능
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
    }

    // ========================================
    // Getters
    // ========================================

    public Long getId() {
        return id;
    }

    public String getOpId() {
        return opId;
    }

    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

    public String getDomain() {
        return commandInfo.domain();
    }

    public String getEventType() {
        return commandInfo.eventType();
    }

    public String getBizKey() {
        return commandInfo.bizKey();
    }

    public String getIdemKey() {
        return commandInfo.idemKey();
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getPayload() {
        return payload;
    }

    public OperationState getOperationState() {
        return operationState;
    }

    public WriteAheadState getWalState() {
        return walState;
    }

    public String getOutcomeJson() {
        return outcomeJson;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
