package com.ryuqq.crawlinghub.domain.crawl.schedule.outbox;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Seller Crawl Schedule Outbox Domain 모델
 *
 * <p>Outbox Pattern / Write-Ahead Log (WAL)을 위한 Domain 객체입니다.
 * EventBridge 등록/수정/삭제 작업을 DB에 먼저 기록하고, 별도 프로세스에서 처리합니다.
 *
 * <p>주요 책임:
 * <ul>
 *   <li>EventBridge 작업 페이로드 저장</li>
 *   <li>멱등성(Idempotency) 보장</li>
 *   <li>재시도 로직 관리</li>
 *   <li>WAL 상태 관리</li>
 * </ul>
 *
 * @author 개발자
 * @since 2024-01-01
 */
public class SellerCrawlScheduleOutbox {

    private final Long id;
    private String opId;
    private final Long sellerId;
    private final String idemKey;
    private final String domain;
    private final String eventType;
    private final String bizKey;
    private final String payload;
    private String outcomeJson;
    private OperationState operationState;
    private WriteAheadState walState;
    private String errorMessage;
    private Integer retryCount;
    private final Integer maxRetries;
    private final Long timeoutMillis;
    private LocalDateTime completedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private SellerCrawlScheduleOutbox(
        Long id,
        String opId,
        Long sellerId,
        String idemKey,
        String domain,
        String eventType,
        String bizKey,
        String payload,
        String outcomeJson,
        OperationState operationState,
        WriteAheadState walState,
        String errorMessage,
        Integer retryCount,
        Integer maxRetries,
        Long timeoutMillis,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.opId = opId;
        this.sellerId = sellerId;
        this.idemKey = idemKey;
        this.domain = domain;
        this.eventType = eventType;
        this.bizKey = bizKey;
        this.payload = payload;
        this.outcomeJson = outcomeJson;
        this.operationState = operationState;
        this.walState = walState;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.timeoutMillis = timeoutMillis;
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
    public static SellerCrawlScheduleOutbox forEventBridgeRegistration(
        Long sellerId,
        String payload,
        String idemKey
    ) {
        validateRequiredFields(sellerId, payload, idemKey);

        LocalDateTime now = LocalDateTime.now();
        return new SellerCrawlScheduleOutbox(
            null, // ID는 저장 시 생성
            null, // opId는 초기 null
            sellerId,
            idemKey,
            "SELLER_CRAWL_SCHEDULE",
            "EVENTBRIDGE_REGISTER",
            "schedule-" + sellerId,
            payload,
            null, // outcome는 처리 후 저장
            OperationState.PENDING,
            WriteAheadState.PENDING,
            null,
            0, // 초기 재시도 횟수
            3, // 최대 재시도 3회
            60000L, // 타임아웃 60초
            null,
            now,
            now
        );
    }

    /**
     * EventBridge 수정을 위한 Outbox 생성
     */
    public static SellerCrawlScheduleOutbox forEventBridgeUpdate(
        Long sellerId,
        String payload,
        String idemKey
    ) {
        validateRequiredFields(sellerId, payload, idemKey);

        LocalDateTime now = LocalDateTime.now();
        return new SellerCrawlScheduleOutbox(
            null,
            null,
            sellerId,
            idemKey,
            "SELLER_CRAWL_SCHEDULE",
            "EVENTBRIDGE_UPDATE",
            "schedule-" + sellerId,
            payload,
            null,
            OperationState.PENDING,
            WriteAheadState.PENDING,
            null,
            0,
            3,
            60000L,
            null,
            now,
            now
        );
    }

    /**
     * DB reconstitute
     */
    public static SellerCrawlScheduleOutbox reconstitute(
        Long id,
        String opId,
        Long sellerId,
        String idemKey,
        String domain,
        String eventType,
        String bizKey,
        String payload,
        String outcomeJson,
        OperationState operationState,
        WriteAheadState walState,
        String errorMessage,
        Integer retryCount,
        Integer maxRetries,
        Long timeoutMillis,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new SellerCrawlScheduleOutbox(
            id, opId, sellerId, idemKey, domain, eventType, bizKey, payload,
            outcomeJson, operationState, walState, errorMessage, retryCount,
            maxRetries, timeoutMillis, completedAt, createdAt, updatedAt
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
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 타임아웃 확인
     */
    public boolean isTimeout() {
        if (this.createdAt == null || this.timeoutMillis == null) {
            return false;
        }
        LocalDateTime timeoutDeadline = this.createdAt.plusSeconds(this.timeoutMillis / 1000);
        return LocalDateTime.now().isAfter(timeoutDeadline);
    }

    /**
     * 타임아웃 처리
     */
    public void markTimeout() {
        this.operationState = OperationState.FAILED;
        this.errorMessage = "타임아웃: " + timeoutMillis + "ms 초과";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return this.operationState == OperationState.FAILED
            && this.retryCount < this.maxRetries;
    }

    /**
     * 재시도를 위한 재설정 (FAILED → PENDING)
     */
    public void resetForRetry() {
        if (!canRetry()) {
            throw new IllegalStateException(
                "재시도 불가: retryCount=" + retryCount + ", maxRetries=" + maxRetries
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

    public String getDomain() {
        return domain;
    }

    public String getEventType() {
        return eventType;
    }

    public String getBizKey() {
        return bizKey;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public Long getTimeoutMillis() {
        return timeoutMillis;
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
        SellerCrawlScheduleOutbox that = (SellerCrawlScheduleOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SellerCrawlScheduleOutbox{" +
            "id=" + id +
            ", sellerId=" + sellerId +
            ", idemKey='" + idemKey + '\'' +
            ", operationState=" + operationState +
            ", walState=" + walState +
            ", retryCount=" + retryCount +
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
