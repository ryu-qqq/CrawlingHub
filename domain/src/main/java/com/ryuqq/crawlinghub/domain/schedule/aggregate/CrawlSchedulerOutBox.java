package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.util.Objects;

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

    private final CrawlSchedulerOutBoxId outBoxId;
    private final CrawlSchedulerHistoryId historyId;
    private CrawlSchedulerOubBoxStatus status;
    private final Long schedulerId;
    private final Long sellerId;
    private final String schedulerName;
    private final String cronExpression;
    private final SchedulerStatus schedulerStatus;
    private String errorMessage;
    private Long version;

    private final Instant createdAt;
    private Instant processedAt;

    /**
     * 신규 생성 (Auto Increment ID)
     *
     * @param historyId 스케줄러 히스토리 ID
     * @param schedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param schedulerStatus 스케줄러 상태
     * @param now 현재 시각
     * @return 신규 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox forNew(
            CrawlSchedulerHistoryId historyId,
            Long schedulerId,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            SchedulerStatus schedulerStatus,
            Instant now) {
        return new CrawlSchedulerOutBox(
                null,
                historyId,
                CrawlSchedulerOubBoxStatus.PENDING,
                schedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                schedulerStatus,
                null,
                0L,
                now,
                null);
    }

    /**
     * 영속성 복원 (Mapper 전용)
     *
     * @param outBoxId 아웃박스 ID
     * @param historyId 스케줄러 히스토리 ID
     * @param status 아웃박스 상태
     * @param schedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param schedulerStatus 스케줄러 상태
     * @param errorMessage 에러 메시지
     * @param version 버전 (Optimistic Locking)
     * @param createdAt 생성 시각
     * @param processedAt 처리 시각
     * @return CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox reconstitute(
            CrawlSchedulerOutBoxId outBoxId,
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerOubBoxStatus status,
            Long schedulerId,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            SchedulerStatus schedulerStatus,
            String errorMessage,
            Long version,
            Instant createdAt,
            Instant processedAt) {
        return new CrawlSchedulerOutBox(
                outBoxId,
                historyId,
                status,
                schedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                schedulerStatus,
                errorMessage,
                version,
                createdAt,
                processedAt);
    }

    private CrawlSchedulerOutBox(
            CrawlSchedulerOutBoxId outBoxId,
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerOubBoxStatus status,
            Long schedulerId,
            Long sellerId,
            String schedulerName,
            String cronExpression,
            SchedulerStatus schedulerStatus,
            String errorMessage,
            Long version,
            Instant createdAt,
            Instant processedAt) {
        this.outBoxId = outBoxId;
        this.historyId = historyId;
        this.status = status;
        this.schedulerId = schedulerId;
        this.sellerId = sellerId;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
        this.schedulerStatus = schedulerStatus;
        this.errorMessage = errorMessage;
        this.version = version;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 처리 시작 (PENDING → PROCESSING)
     *
     * @param now 현재 시각
     */
    public void markAsProcessing(Instant now) {
        if (this.status != CrawlSchedulerOubBoxStatus.PENDING) {
            throw new IllegalStateException(
                    "PENDING 상태에서만 PROCESSING으로 전환할 수 있습니다. 현재: " + this.status);
        }
        this.status = CrawlSchedulerOubBoxStatus.PROCESSING;
        this.processedAt = now;
    }

    /**
     * AWS EventBridge 동기화 완료
     *
     * @param now 현재 시각
     */
    public void markAsCompleted(Instant now) {
        if (this.status == CrawlSchedulerOubBoxStatus.COMPLETED) {
            return;
        }
        this.status = CrawlSchedulerOubBoxStatus.COMPLETED;
        this.processedAt = now;
        this.errorMessage = null;
    }

    /**
     * AWS EventBridge 동기화 실패
     *
     * @param errorMessage 에러 메시지
     * @param now 현재 시각
     */
    public void markAsFailed(String errorMessage, Instant now) {
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("에러 메시지는 null이거나 빈 문자열일 수 없습니다.");
        }
        this.status = CrawlSchedulerOubBoxStatus.FAILED;
        this.processedAt = now;
        this.errorMessage = errorMessage;
    }

    /** 재시도를 위해 PENDING 상태로 복원 (FAILED → PENDING) */
    public void retry() {
        if (this.status != CrawlSchedulerOubBoxStatus.FAILED) {
            throw new IllegalStateException("FAILED 상태에서만 재시도할 수 있습니다.");
        }
        this.status = CrawlSchedulerOubBoxStatus.PENDING;
        this.processedAt = null;
        this.errorMessage = null;
    }

    /** 좀비 복구를 위해 PENDING 상태로 복원 (PROCESSING → PENDING) */
    public void resetToPending() {
        if (this.status != CrawlSchedulerOubBoxStatus.PROCESSING) {
            throw new IllegalStateException(
                    "PROCESSING 상태에서만 PENDING으로 복원할 수 있습니다. 현재: " + this.status);
        }
        this.status = CrawlSchedulerOubBoxStatus.PENDING;
        this.processedAt = null;
        this.errorMessage = null;
    }

    // ==================== Getter ====================

    public CrawlSchedulerOutBoxId getOutBoxId() {
        return outBoxId;
    }

    public Long getOutBoxIdValue() {
        return outBoxId != null ? outBoxId.value() : null;
    }

    public CrawlSchedulerHistoryId getHistoryId() {
        return historyId;
    }

    public Long getHistoryIdValue() {
        return historyId.value();
    }

    public CrawlSchedulerOubBoxStatus getStatus() {
        return status;
    }

    public Long getSchedulerId() {
        return schedulerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public SchedulerStatus getSchedulerStatus() {
        return schedulerStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isCompleted() {
        return this.status == CrawlSchedulerOubBoxStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == CrawlSchedulerOubBoxStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == CrawlSchedulerOubBoxStatus.PENDING;
    }

    public boolean isProcessing() {
        return this.status == CrawlSchedulerOubBoxStatus.PROCESSING;
    }

    // ==================== equals/hashCode (ID 기반) ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrawlSchedulerOutBox that = (CrawlSchedulerOutBox) o;
        return Objects.equals(outBoxId, that.outBoxId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outBoxId);
    }
}
