package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOutBoxId;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 크롤 스케줄러 아웃박스 Aggregate Root
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>AWS EventBridge 동기화를 위한 아웃박스 패턴
 *   <li>상태: PENDING (대기) → COMPLETED (완료) / FAILED (실패)
 *   <li>재시도 로직은 Application Layer에서 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlSchedulerOutBox {

    // ==================== 필드 ====================

    private final CrawlSchedulerOutBoxId outBoxId;
    private final CrawlSchedulerHistoryId historyId;
    private CrawlSchedulerOubBoxStatus status;
    private String eventPayload; // JSON 형태의 이벤트 데이터
    private String errorMessage; // 실패 시 에러 메시지
    private Long version; // Optimistic Locking

    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private final Clock clock;

    // ==================== 생성 메서드 (3종) ====================

    /**
     * 신규 생성 (Auto Increment ID)
     *
     * @param historyId 스케줄러 히스토리 ID
     * @param eventPayload 이벤트 페이로드 (JSON)
     * @param clock 시간 제어
     * @return 신규 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox forNew(
            CrawlSchedulerHistoryId historyId, String eventPayload, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new CrawlSchedulerOutBox(
                null, // Auto Increment: ID null
                historyId,
                CrawlSchedulerOubBoxStatus.PENDING,
                eventPayload,
                null, // errorMessage null
                0L, // version 초기값
                now,
                null, // processedAt null
                clock);
    }

    /**
     * ID 기반 생성 (비즈니스 로직용)
     *
     * @param outBoxId 아웃박스 ID (null 불가)
     * @param historyId 스케줄러 히스토리 ID
     * @param status 아웃박스 상태
     * @param eventPayload 이벤트 페이로드 (JSON)
     * @param errorMessage 에러 메시지
     * @param version 버전 (Optimistic Locking)
     * @param createdAt 생성 시각
     * @param processedAt 처리 시각
     * @param clock 시간 제어
     * @return CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox of(
            CrawlSchedulerOutBoxId outBoxId,
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerOubBoxStatus status,
            String eventPayload,
            String errorMessage,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            Clock clock) {
        if (outBoxId == null) {
            throw new IllegalArgumentException("outBoxId는 null일 수 없습니다.");
        }
        return new CrawlSchedulerOutBox(
                outBoxId,
                historyId,
                status,
                eventPayload,
                errorMessage,
                version,
                createdAt,
                processedAt,
                clock);
    }

    /**
     * 영속성 복원 (Mapper 전용)
     *
     * @param outBoxId 아웃박스 ID (null 불가)
     * @param historyId 스케줄러 히스토리 ID
     * @param status 아웃박스 상태
     * @param eventPayload 이벤트 페이로드 (JSON)
     * @param errorMessage 에러 메시지
     * @param version 버전 (Optimistic Locking)
     * @param createdAt 생성 시각
     * @param processedAt 처리 시각
     * @param clock 시간 제어
     * @return CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox reconstitute(
            CrawlSchedulerOutBoxId outBoxId,
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerOubBoxStatus status,
            String eventPayload,
            String errorMessage,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            Clock clock) {
        if (outBoxId == null) {
            throw new IllegalArgumentException("outBoxId는 null일 수 없습니다.");
        }
        return new CrawlSchedulerOutBox(
                outBoxId,
                historyId,
                status,
                eventPayload,
                errorMessage,
                version,
                createdAt,
                processedAt,
                clock);
    }

    /** 생성자 (private) */
    private CrawlSchedulerOutBox(
            CrawlSchedulerOutBoxId outBoxId,
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerOubBoxStatus status,
            String eventPayload,
            String errorMessage,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            Clock clock) {
        this.outBoxId = outBoxId;
        this.historyId = historyId;
        this.status = status;
        this.eventPayload = eventPayload;
        this.errorMessage = errorMessage;
        this.version = version;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.clock = clock;
    }

    // ==================== 비즈니스 메서드 ====================

    /** AWS EventBridge 동기화 완료 */
    public void markAsCompleted() {
        if (this.status == CrawlSchedulerOubBoxStatus.COMPLETED) {
            return; // 이미 완료 상태면 무시
        }
        this.status = CrawlSchedulerOubBoxStatus.COMPLETED;
        this.processedAt = LocalDateTime.now(clock);
        this.errorMessage = null;
    }

    /**
     * AWS EventBridge 동기화 실패
     *
     * @param errorMessage 에러 메시지
     */
    public void markAsFailed(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("에러 메시지는 null이거나 빈 문자열일 수 없습니다.");
        }
        this.status = CrawlSchedulerOubBoxStatus.FAILED;
        this.processedAt = LocalDateTime.now(clock);
        this.errorMessage = errorMessage;
    }

    /** 재시도를 위해 PENDING 상태로 복원 */
    public void retry() {
        if (this.status != CrawlSchedulerOubBoxStatus.FAILED) {
            throw new IllegalStateException("FAILED 상태에서만 재시도할 수 있습니다.");
        }
        this.status = CrawlSchedulerOubBoxStatus.PENDING;
        this.processedAt = null;
        this.errorMessage = null;
    }

    // ==================== Getter ====================

    public CrawlSchedulerOutBoxId getOutBoxId() {
        return outBoxId;
    }

    /** Law of Demeter: 원시 타입이 필요한 경우 별도 메서드 제공 */
    public Long getOutBoxIdValue() {
        return outBoxId != null ? outBoxId.value() : null;
    }

    public CrawlSchedulerHistoryId getHistoryId() {
        return historyId;
    }

    /** Law of Demeter: 히스토리 ID의 원시값 */
    public Long getHistoryIdValue() {
        return historyId.value();
    }

    public CrawlSchedulerOubBoxStatus getStatus() {
        return status;
    }

    public String getEventPayload() {
        return eventPayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public Long getVersion() {
        return version;
    }

    /** 완료 상태 여부 */
    public boolean isCompleted() {
        return this.status == CrawlSchedulerOubBoxStatus.COMPLETED;
    }

    /** 실패 상태 여부 */
    public boolean isFailed() {
        return this.status == CrawlSchedulerOubBoxStatus.FAILED;
    }

    /** 대기 상태 여부 */
    public boolean isPending() {
        return this.status == CrawlSchedulerOubBoxStatus.PENDING;
    }
}
