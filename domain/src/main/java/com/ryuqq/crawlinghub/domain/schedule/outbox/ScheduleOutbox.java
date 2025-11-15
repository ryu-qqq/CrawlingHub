package com.ryuqq.crawlinghub.domain.schedule.outbox;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Schedule Outbox Domain 모델
 *
 * <p>Outbox Pattern / Write-Ahead Log (WAL)을 위한 Domain 객체입니다.
 * EventBridge 등록/수정/삭제 작업을 DB에 먼저 기록하고, 별도 프로세스에서 처리합니다.
 *
 * <p>주요 책임:
 * <ul>
 *   <li>EventBridge 작업 페이로드 저장</li>
 *   <li>멱등성(Idempotency) 보장</li>
 *   <li>재시도 로직 관리 (RetryPolicy VO 사용)</li>
 *   <li>WAL 상태 관리</li>
 * </ul>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class ScheduleOutbox {

    /**
     * 도메인 상수 (모든 ScheduleOutbox는 동일한 도메인)
     */
    private static final String DOMAIN = "SELLER_CRAWL_SCHEDULE";

    private final Long id;
    private String opId;
    private final Long sellerId;
    private final String idemKey;
    private final EventType eventType;
    private final String payload;
    private String outcomeJson;
    private OperationState operationState;
    private WriteAheadState walState;
    private String errorMessage;
    private RetryPolicy retryPolicy;
    private LocalDateTime completedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private ScheduleOutbox(
        Long id,
        String opId,
        Long sellerId,
        String idemKey,
        EventType eventType,
        String payload,
        String outcomeJson,
        OperationState operationState,
        WriteAheadState walState,
        String errorMessage,
        RetryPolicy retryPolicy,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.opId = opId;
        this.sellerId = sellerId;
        this.idemKey = idemKey;
        this.eventType = eventType;
        this.payload = payload;
        this.outcomeJson = outcomeJson;
        this.operationState = operationState;
        this.walState = walState;
        this.errorMessage = errorMessage;
        this.retryPolicy = retryPolicy;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * EventBridge 등록을 위한 Outbox 생성 (신규)
     *
     * @param sellerId 셀러 ID
     * @param payload EventBridge 페이로드 JSON
     * @param idemKey 멱등성 키
     * @return Outbox 인스턴스
     */
    public static ScheduleOutbox forEventBridgeRegistration(
        Long sellerId,
        String payload,
        String idemKey
    ) {
        validateRequiredFields(sellerId, payload, idemKey);

        LocalDateTime now = LocalDateTime.now();
        return new ScheduleOutbox(
            null, // ID는 저장 시 생성
            null, // opId는 초기 null
            sellerId,
            idemKey,
            EventType.EVENTBRIDGE_REGISTER,
            payload,
            null, // outcome는 처리 후 저장
            OperationState.PENDING,
            WriteAheadState.PENDING,
            null,
            RetryPolicy.createDefault(),
            null,
            now,
            now
        );
    }

    /**
     * EventBridge 수정을 위한 Outbox 생성
     */
    public static ScheduleOutbox forEventBridgeUpdate(
        Long sellerId,
        String payload,
        String idemKey
    ) {
        validateRequiredFields(sellerId, payload, idemKey);

        LocalDateTime now = LocalDateTime.now();
        return new ScheduleOutbox(
            null,
            null,
            sellerId,
            idemKey,
            EventType.EVENTBRIDGE_UPDATE,
            payload,
            null,
            OperationState.PENDING,
            WriteAheadState.PENDING,
            null,
            RetryPolicy.createDefault(),
            null,
            now,
            now
        );
    }

    /**
     * DB reconstitute
     */
    public static ScheduleOutbox reconstitute(
        Long id,
        String opId,
        Long sellerId,
        String idemKey,
        EventType eventType,
        String payload,
        String outcomeJson,
        OperationState operationState,
        WriteAheadState walState,
        String errorMessage,
        RetryPolicy retryPolicy,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new ScheduleOutbox(
            id, opId, sellerId, idemKey, eventType, payload,
            outcomeJson, operationState, walState, errorMessage, retryPolicy,
            completedAt, createdAt, updatedAt
        );
    }

    private static void validateRequiredFields(Long sellerId, String payload, String idemKey) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID는 필수입니다");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload는 필수입니다");
        }
        if (idemKey == null || idemKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency Key는 필수입니다");
        }
    }

    /**
     * 처리 시작 (PENDING → IN_PROGRESS)
     */
    public void startProcessing() {
        if (this.operationState != OperationState.PENDING) {
            throw new IllegalStateException(
                "PENDING 상태에서만 처리 시작 가능합니다. 현재: " + this.operationState
            );
        }
        this.operationState = OperationState.IN_PROGRESS;
        this.opId = UUID.randomUUID().toString();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 처리 완료 (IN_PROGRESS → COMPLETED)
     */
    public void markCompleted() {
        this.operationState = OperationState.COMPLETED;
        this.walState = WriteAheadState.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 실패 기록 (IN_PROGRESS → FAILED)
     */
    public void recordFailure(String errorMessage) {
        this.operationState = OperationState.FAILED;
        this.errorMessage = errorMessage;
        this.retryPolicy = retryPolicy.incrementRetry();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 타임아웃 확인
     */
    public boolean isTimeout() {
        return retryPolicy.isTimeout(this.createdAt);
    }

    /**
     * 타임아웃 처리
     */
    public void markTimeout() {
        this.operationState = OperationState.FAILED;
        this.errorMessage = "타임아웃: " + retryPolicy.timeoutMillis() + "ms 초과";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return this.operationState == OperationState.FAILED
            && retryPolicy.canRetry();
    }

    /**
     * 재시도를 위한 재설정 (FAILED → PENDING)
     */
    public void resetForRetry() {
        if (!canRetry()) {
            throw new IllegalStateException(
                "재시도 불가: retryCount=" + retryPolicy.retryCount()
                + ", maxRetries=" + retryPolicy.maxRetries()
            );
        }
        this.operationState = OperationState.PENDING;
        this.errorMessage = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 정리 대상 여부 (완료 후 일정 시간 경과)
     *
     * @param retentionHours 보관 시간 (시간 단위)
     * @return 정리 대상 여부
     */
    public boolean isOldEnough(int retentionHours) {
        if (this.completedAt == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().minusHours(retentionHours);
        return this.completedAt.isBefore(threshold);
    }

    /**
     * 비즈니스 키 동적 생성
     *
     * @return 비즈니스 키
     */
    public String getBizKey() {
        return "schedule-" + sellerId;
    }

    /**
     * 도메인 상수 반환
     *
     * @return 도메인
     */
    public String getDomain() {
        return DOMAIN;
    }

    // Getters (Law of Demeter 준수)
    public Long getId() {
        return id;
    }

    public String getOpId() {
        return opId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getOutcomeJson() {
        return outcomeJson;
    }

    public OperationState getOperationState() {
        return operationState;
    }

    public WriteAheadState getWalState() {
        return walState;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * 현재 재시도 횟수 반환 (Law of Demeter 준수)
     */
    public int getRetryCount() {
        return retryPolicy.retryCount();
    }

    /**
     * 최대 재시도 횟수 반환 (Law of Demeter 준수)
     */
    public int getMaxRetries() {
        return retryPolicy.maxRetries();
    }

    /**
     * 타임아웃 시간(ms) 반환 (Law of Demeter 준수)
     */
    public long getTimeoutMillis() {
        return retryPolicy.timeoutMillis();
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScheduleOutbox that = (ScheduleOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ScheduleOutbox{" +
            "id=" + id +
            ", sellerId=" + sellerId +
            ", idemKey='" + idemKey + '\'' +
            ", eventType=" + eventType +
            ", operationState=" + operationState +
            ", walState=" + walState +
            ", retryPolicy=" + retryPolicy +
            '}';
    }

    /**
     * 작업 상태 Enum
     */
    public enum OperationState {
        /** 대기 중 */
        PENDING,
        /** 처리 중 */
        IN_PROGRESS,
        /** 완료 */
        COMPLETED,
        /** 실패 */
        FAILED
    }

    /**
     * Write-Ahead Log 상태 Enum
     */
    public enum WriteAheadState {
        /** WAL 대기 중 */
        PENDING,
        /** WAL 완료 */
        COMPLETED
    }
}
