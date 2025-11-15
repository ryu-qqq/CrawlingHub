package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity;

import jakarta.persistence.*;

import com.ryuqq.crawlinghub.domain.task.TaskStatus;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import com.ryuqq.crawlinghub.domain.task.TriggerType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 크롤링 작업 JPA Entity
 *
 * <p>테이블: crawl_task</p>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java</li>
 *   <li>✅ 3-생성자 패턴: no-args, create, reconstitute</li>
 *   <li>✅ Long FK 전략 - sellerId, crawlScheduleId는 Long 타입</li>
 *   <li>✅ final 필드 금지 - JPA Reflection 요구사항</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Entity
@Table(
    name = "crawl_task",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idempotency_key"})
    },
    indexes = {
        @Index(name = "idx_seller_id", columnList = "seller_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_scheduled_at", columnList = "scheduled_at"),
        @Index(name = "idx_crawl_schedule_id", columnList = "crawl_schedule_id")
    }
)
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "seller_name", nullable = false, length = 255)
    private String sellerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 50)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "request_url", nullable = false, length = 1000)
    private String requestUrl;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "idempotency_key", nullable = false, length = 255, unique = true)
    private String idempotencyKey;

    @Column(name = "crawl_schedule_id")
    private Long crawlScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private TriggerType triggerType;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * No-args 생성자 (JPA 필수)
     */
    protected TaskEntity() {
        this.id = null;
        this.sellerId = null;
        this.sellerName = null;
        this.taskType = null;
        this.status = null;
        this.requestUrl = null;
        this.pageNumber = null;
        this.retryCount = null;
        this.idempotencyKey = null;
        this.crawlScheduleId = null;
        this.triggerType = null;
        this.scheduledAt = null;
        this.startedAt = null;
        this.completedAt = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    /**
     * 신규 생성용 생성자 (ID 없음)
     *
     * @param sellerId 셀러 ID
     * @param sellerName 셀러 이름
     * @param taskType 작업 타입
     * @param status 작업 상태
     * @param requestUrl 요청 URL
     * @param pageNumber 페이지 번호
     * @param retryCount 재시도 횟수
     * @param idempotencyKey 멱등성 키
     * @param crawlScheduleId 크롤 스케줄 ID
     * @param triggerType 트리거 타입
     * @param scheduledAt 예약 시각
     */
    protected TaskEntity(
        Long sellerId,
        String sellerName,
        TaskType taskType,
        TaskStatus status,
        String requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt
    ) {
        this.id = null;
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.sellerName = Objects.requireNonNull(sellerName, "sellerName must not be null");
        this.taskType = Objects.requireNonNull(taskType, "taskType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.requestUrl = Objects.requireNonNull(requestUrl, "requestUrl must not be null");
        this.pageNumber = pageNumber;
        this.retryCount = Objects.requireNonNull(retryCount, "retryCount must not be null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        this.crawlScheduleId = crawlScheduleId;
        this.triggerType = Objects.requireNonNull(triggerType, "triggerType must not be null");
        this.scheduledAt = Objects.requireNonNull(scheduledAt, "scheduledAt must not be null");
        this.startedAt = null;
        this.completedAt = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Static Factory Method - 신규 생성
     *
     * @param sellerId 셀러 ID
     * @param sellerName 셀러 이름
     * @param taskType 작업 타입
     * @param status 작업 상태
     * @param requestUrl 요청 URL
     * @param pageNumber 페이지 번호
     * @param retryCount 재시도 횟수
     * @param idempotencyKey 멱등성 키
     * @param crawlScheduleId 크롤 스케줄 ID
     * @param triggerType 트리거 타입
     * @param scheduledAt 예약 시각
     * @return TaskEntity
     */
    public static TaskEntity create(
        Long sellerId,
        String sellerName,
        TaskType taskType,
        TaskStatus status,
        String requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt
    ) {
        return new TaskEntity(
            sellerId,
            sellerName,
            taskType,
            status,
            requestUrl,
            pageNumber,
            retryCount,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt
        );
    }

    /**
     * DB reconstitute용 전체 생성자
     *
     * @param id ID
     * @param sellerId 셀러 ID
     * @param sellerName 셀러 이름
     * @param taskType 작업 타입
     * @param status 작업 상태
     * @param requestUrl 요청 URL
     * @param pageNumber 페이지 번호
     * @param retryCount 재시도 횟수
     * @param idempotencyKey 멱등성 키
     * @param crawlScheduleId 크롤 스케줄 ID
     * @param triggerType 트리거 타입
     * @param scheduledAt 예약 시각
     * @param startedAt 시작 시각
     * @param completedAt 완료 시각
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     */
    private TaskEntity(
        Long id,
        Long sellerId,
        String sellerName,
        TaskType taskType,
        TaskStatus status,
        String requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.sellerName = Objects.requireNonNull(sellerName, "sellerName must not be null");
        this.taskType = Objects.requireNonNull(taskType, "taskType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.requestUrl = Objects.requireNonNull(requestUrl, "requestUrl must not be null");
        this.pageNumber = pageNumber;
        this.retryCount = Objects.requireNonNull(retryCount, "retryCount must not be null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        this.crawlScheduleId = crawlScheduleId;
        this.triggerType = Objects.requireNonNull(triggerType, "triggerType must not be null");
        this.scheduledAt = Objects.requireNonNull(scheduledAt, "scheduledAt must not be null");
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Static Factory Method - DB reconstitute
     *
     * @param id ID
     * @param sellerId 셀러 ID
     * @param sellerName 셀러 이름
     * @param taskType 작업 타입
     * @param status 작업 상태
     * @param requestUrl 요청 URL
     * @param pageNumber 페이지 번호
     * @param retryCount 재시도 횟수
     * @param idempotencyKey 멱등성 키
     * @param crawlScheduleId 크롤 스케줄 ID
     * @param triggerType 트리거 타입
     * @param scheduledAt 예약 시각
     * @param startedAt 시작 시각
     * @param completedAt 완료 시각
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return TaskEntity
     */
    public static TaskEntity reconstitute(
        Long id,
        Long sellerId,
        String sellerName,
        TaskType taskType,
        TaskStatus status,
        String requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new TaskEntity(
            id,
            sellerId,
            sellerName,
            taskType,
            status,
            requestUrl,
            pageNumber,
            retryCount,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt,
            startedAt,
            completedAt,
            createdAt,
            updatedAt
        );
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getRequestUrl() {
        return requestUrl;
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

    public Long getCrawlScheduleId() {
        return crawlScheduleId;
    }

    public TriggerType getTriggerType() {
        return triggerType;
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

    // Setters (for JPA state management)

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskEntity that = (TaskEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
            "id=" + id +
            ", sellerId=" + sellerId +
            ", sellerName='" + sellerName + '\'' +
            ", taskType='" + taskType + '\'' +
            ", status='" + status + '\'' +
            ", retryCount=" + retryCount +
            ", idempotencyKey='" + idempotencyKey + '\'' +
            ", crawlScheduleId=" + crawlScheduleId +
            ", triggerType='" + triggerType + '\'' +
            ", scheduledAt=" + scheduledAt +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
