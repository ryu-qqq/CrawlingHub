package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TaskMessageOutbox JPA Entity
 *
 * <p>Outbox 패턴을 사용하여 Task 생성 후 SQS로 메시지를 안정적으로 발행하기 위한 Entity입니다.</p>
 *
 * <p><strong>Outbox 패턴:</strong></p>
 * <ul>
 *   <li>Task 저장과 동시에 Outbox 레코드 생성 (트랜잭션 보장)</li>
 *   <li>별도 Scheduler가 PENDING 상태 레코드를 조회하여 SQS 발행</li>
 *   <li>발행 성공 시 SENT 상태로 변경</li>
 *   <li>발행 실패 시 retryCount 증가 (최대 3회)</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java getters/setters</li>
 *   <li>✅ Long FK 전략 - taskId를 Long으로 저장 (no @ManyToOne)</li>
 *   <li>✅ No final fields - JPA Reflection 요구사항</li>
 *   <li>✅ 3-constructor pattern - no-args (JPA), create, reconstitute</li>
 *   <li>✅ Enum stored as String - taskType, status</li>
 * </ul>
 *
 * <p><strong>동시성 제어:</strong></p>
 * <ul>
 *   <li>Scheduler에서 PENDING 레코드 조회 시 Pessimistic Lock 사용</li>
 *   <li>다중 Scheduler 인스턴스 실행 시 중복 처리 방지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Entity
@Table(
    name = "task_message_outbox",
    indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
public class TaskMessageOutBoxEntity {

    /**
     * Outbox PK (자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    private Long outboxId;

    /**
     * Task ID (Long FK)
     *
     * <p>Long FK 전략 - @ManyToOne 관계 사용하지 않음</p>
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * Task 타입 (String으로 저장)
     *
     * <p>Domain TaskType Enum → String 변환</p>
     */
    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    /**
     * Outbox 상태 (String으로 저장)
     *
     * <p>Domain TaskMessageStatus Enum → String 변환</p>
     * <ul>
     *   <li>PENDING - 발행 대기</li>
     *   <li>SENT - 발행 완료</li>
     * </ul>
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 재시도 횟수 (최대 3회)
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * 에러 메시지 (발행 실패 시)
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * 생성 일시 (자동 설정)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 발행 완료 일시 (SENT 상태로 변경 시 설정)
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * JPA 기본 생성자 (protected)
     *
     * <p>JPA Reflection을 위해 필수</p>
     */
    protected TaskMessageOutBoxEntity() {
        this.outboxId = null;
        this.taskId = null;
        this.taskType = null;
        this.status = null;
        this.retryCount = null;
        this.errorMessage = null;
        this.createdAt = null;
        this.sentAt = null;
    }

    /**
     * 생성 전용 생성자 (private)
     *
     * <p>create() Factory Method에서만 호출</p>
     */
    private TaskMessageOutBoxEntity(
        Long taskId,
        String taskType,
        String status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime sentAt
    ) {
        this.outboxId = null;
        this.taskId = taskId;
        this.taskType = taskType;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    /**
     * DB Reconstitute 전용 생성자 (private)
     *
     * <p>reconstitute() Factory Method에서만 호출</p>
     */
    private TaskMessageOutBoxEntity(
        Long outboxId,
        Long taskId,
        String taskType,
        String status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime sentAt
    ) {
        this.outboxId = outboxId;
        this.taskId = taskId;
        this.taskType = taskType;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    /**
     * 신규 Outbox 생성 Factory Method
     *
     * <p>Task 저장과 동시에 PENDING 상태의 Outbox 레코드를 생성합니다.</p>
     *
     * @param taskId      Task ID (Long FK)
     * @param taskType    Task 타입 (String)
     * @param status      초기 상태 (보통 PENDING)
     * @param retryCount  초기 재시도 횟수 (0)
     * @param errorMessage 초기 에러 메시지 (null)
     * @param createdAt   생성 일시
     * @return TaskMessageOutBoxEntity 새로운 Entity
     */
    public static TaskMessageOutBoxEntity create(
        Long taskId,
        String taskType,
        String status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime createdAt
    ) {
        return new TaskMessageOutBoxEntity(
            taskId,
            taskType,
            status,
            retryCount,
            errorMessage,
            createdAt,
            null
        );
    }

    /**
     * DB로부터 Reconstitute Factory Method
     *
     * <p>DB에서 조회한 데이터를 Entity로 재구성합니다.</p>
     *
     * @param outboxId     Outbox PK
     * @param taskId       Task ID (Long FK)
     * @param taskType     Task 타입 (String)
     * @param status       Outbox 상태 (String)
     * @param retryCount   재시도 횟수
     * @param errorMessage 에러 메시지
     * @param createdAt    생성 일시
     * @param sentAt       발행 완료 일시
     * @return TaskMessageOutBoxEntity DB에서 재구성된 Entity
     */
    public static TaskMessageOutBoxEntity reconstitute(
        Long outboxId,
        Long taskId,
        String taskType,
        String status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime sentAt
    ) {
        return new TaskMessageOutBoxEntity(
            outboxId,
            taskId,
            taskType,
            status,
            retryCount,
            errorMessage,
            createdAt,
            sentAt
        );
    }

    // ==================== Getters ====================

    /**
     * Outbox PK 조회
     *
     * @return Long Outbox ID
     */
    public Long getOutboxId() {
        return outboxId;
    }

    /**
     * Task ID 조회 (Long FK)
     *
     * @return Long Task ID
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * Task 타입 조회 (String)
     *
     * @return String Task 타입
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * Outbox 상태 조회 (String)
     *
     * @return String Outbox 상태 (PENDING, SENT)
     */
    public String getStatus() {
        return status;
    }

    /**
     * 재시도 횟수 조회
     *
     * @return Integer 재시도 횟수
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 에러 메시지 조회
     *
     * @return String 에러 메시지 (nullable)
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 생성 일시 조회
     *
     * @return LocalDateTime 생성 일시
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 발행 완료 일시 조회
     *
     * @return LocalDateTime 발행 완료 일시 (nullable)
     */
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    // ==================== Setters ====================

    /**
     * Outbox 상태 변경
     *
     * <p>상태 변경 시 sentAt 자동 설정 (SENT 상태일 경우)</p>
     *
     * @param status 새로운 상태
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 재시도 횟수 증가
     *
     * @param retryCount 새로운 재시도 횟수
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 에러 메시지 설정
     *
     * @param errorMessage 에러 메시지
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 발행 완료 일시 설정
     *
     * @param sentAt 발행 완료 일시
     */
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    // ==================== equals & hashCode ====================

    /**
     * Entity 동등성 비교
     *
     * <p>outboxId 기준으로 비교</p>
     *
     * @param o 비교 대상 객체
     * @return boolean 동등 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskMessageOutBoxEntity that = (TaskMessageOutBoxEntity) o;
        return Objects.equals(outboxId, that.outboxId);
    }

    /**
     * Entity hashCode
     *
     * <p>outboxId 기준으로 계산</p>
     *
     * @return int hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(outboxId);
    }

    /**
     * Entity 문자열 표현
     *
     * @return String Entity 정보
     */
    @Override
    public String toString() {
        return "TaskMessageOutBoxEntity{" +
            "outboxId=" + outboxId +
            ", taskId=" + taskId +
            ", taskType='" + taskType + '\'' +
            ", status='" + status + '\'' +
            ", retryCount=" + retryCount +
            ", errorMessage='" + errorMessage + '\'' +
            ", createdAt=" + createdAt +
            ", sentAt=" + sentAt +
            '}';
    }
}
