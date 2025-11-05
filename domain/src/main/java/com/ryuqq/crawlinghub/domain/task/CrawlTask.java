package com.ryuqq.crawlinghub.domain.crawl.task;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 크롤링 작업 Aggregate Root
 * 
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>최대 재시도 횟수: 3회</li>
 *   <li>타임아웃: 10분</li>
 *   <li>RUNNING 상태 10분 초과 시 자동 RETRY</li>
 *   <li>멱등성 키로 중복 방지</li>
 * </ul>
 */
public class CrawlTask {

    private static final int MAX_RETRY_COUNT = 3;

    private final CrawlTaskId id;
    private final MustitSellerId sellerId;
    private final TaskType taskType;
    private TaskStatus status;
    private final RequestUrl requestUrl;
    private final Integer pageNumber;
    private Integer retryCount;
    private final String idempotencyKey;
    private final LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private CrawlTask(
        CrawlTaskId id,
        MustitSellerId sellerId,
        TaskType taskType,
        TaskStatus status,
        RequestUrl requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.status = status;
        this.requestUrl = requestUrl;
        this.pageNumber = pageNumber;
        this.retryCount = retryCount;
        this.idempotencyKey = idempotencyKey;
        this.scheduledAt = scheduledAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    CrawlTask(
        CrawlTaskId id,
        MustitSellerId sellerId,
        TaskType taskType,
        RequestUrl requestUrl,
        Integer pageNumber,
        String idempotencyKey,
        LocalDateTime scheduledAt,
        Clock clock
    ) {
        validateRequiredFields(sellerId, taskType, requestUrl, idempotencyKey, scheduledAt);

        this.id = id;
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.status = TaskStatus.WAITING;
        this.requestUrl = requestUrl;
        this.pageNumber = pageNumber;
        this.retryCount = 0;
        this.idempotencyKey = idempotencyKey;
        this.scheduledAt = scheduledAt;
        this.startedAt = null;
        this.completedAt = null;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 신규 작업 생성 (ID 없음)
     */
    public static CrawlTask forNew(
        MustitSellerId sellerId,
        TaskType taskType,
        RequestUrl requestUrl,
        Integer pageNumber,
        String idempotencyKey,
        LocalDateTime scheduledAt
    ) {
        return new CrawlTask(
            null,
            sellerId,
            taskType,
            requestUrl,
            pageNumber,
            idempotencyKey,
            scheduledAt,
            Clock.systemDefaultZone()
        );
    }

    /**
     * 기존 작업 생성 (ID 있음)
     */
    public static CrawlTask of(
        CrawlTaskId id,
        MustitSellerId sellerId,
        TaskType taskType,
        RequestUrl requestUrl,
        Integer pageNumber,
        String idempotencyKey,
        LocalDateTime scheduledAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("CrawlTask ID는 필수입니다");
        }
        return new CrawlTask(
            id,
            sellerId,
            taskType,
            requestUrl,
            pageNumber,
            idempotencyKey,
            scheduledAt,
            Clock.systemDefaultZone()
        );
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static CrawlTask reconstitute(
        CrawlTaskId id,
        MustitSellerId sellerId,
        TaskType taskType,
        TaskStatus status,
        RequestUrl requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new CrawlTask(
            id,
            sellerId,
            taskType,
            status,
            requestUrl,
            pageNumber,
            retryCount,
            idempotencyKey,
            scheduledAt,
            startedAt,
            completedAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(
        MustitSellerId sellerId,
        TaskType taskType,
        RequestUrl requestUrl,
        String idempotencyKey,
        LocalDateTime scheduledAt
    ) {
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("작업 유형은 필수입니다");
        }
        if (requestUrl == null) {
            throw new IllegalArgumentException("요청 URL은 필수입니다");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등성 키는 필수입니다");
        }
        if (scheduledAt == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다");
        }
    }

    /**
     * 작업 발행
     */
    public void publish() {
        if (this.status != TaskStatus.WAITING) {
            throw new IllegalStateException("WAITING 상태에서만 발행할 수 있습니다");
        }
        this.status = TaskStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 작업 시작
     */
    public void startProcessing() {
        if (this.status != TaskStatus.PUBLISHED && this.status != TaskStatus.RETRY) {
            throw new IllegalStateException("PUBLISHED 또는 RETRY 상태에서만 시작할 수 있습니다");
        }
        this.status = TaskStatus.RUNNING;
        this.startedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 작업 성공 완료
     */
    public void completeSuccessfully() {
        if (this.status != TaskStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태에서만 완료할 수 있습니다");
        }
        this.status = TaskStatus.SUCCESS;
        this.completedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 작업 실패
     */
    public void failWithError(String errorMessage) {
        if (this.status != TaskStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태에서만 실패 처리할 수 있습니다");
        }

        if (canRetry()) {
            this.status = TaskStatus.RETRY;
            this.retryCount++;
        } else {
            this.status = TaskStatus.FAILED;
            this.completedAt = LocalDateTime.now(clock);
        }
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 재시도 횟수 증가
     */
    public void incrementRetry() {
        if (!canRetry()) {
            throw new IllegalStateException("최대 재시도 횟수를 초과했습니다");
        }
        this.retryCount++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 타임아웃 확인 (10분)
     */
    public boolean isTimeout() {
        if (startedAt == null || !status.isRunning()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return startedAt.plusMinutes(10).isBefore(now);
    }

    /**
     * 특정 상태인지 확인
     */
    public boolean hasStatus(TaskStatus targetStatus) {
        return this.status == targetStatus;
    }

    /**
     * 완료 여부
     */
    public boolean isCompleted() {
        return status.isCompleted();
    }

    /**
     * 실패 여부
     */
    public boolean isFailed() {
        return status.isFailed();
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getRequestUrlValue() {
        return requestUrl.getValue();
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
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
        CrawlTask that = (CrawlTask) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CrawlTask{" +
            "id=" + id +
            ", sellerId=" + sellerId +
            ", taskType=" + taskType +
            ", status=" + status +
            ", requestUrl=" + requestUrl +
            ", pageNumber=" + pageNumber +
            ", retryCount=" + retryCount +
            '}';
    }
}
