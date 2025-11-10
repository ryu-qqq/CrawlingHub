package com.ryuqq.crawlinghub.domain.task.outbox;

import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import com.ryuqq.crawlinghub.domain.task.event.TaskMessageCreatedEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Task 메시지 Outbox (Domain Aggregate Root)
 *
 * <p>Task 생성 후 SQS 발행을 보장하는 Outbox 패턴
 *
 * <p>상태 전이:
 * <ul>
 *   <li>PENDING → SENT (SQS 발행 성공)</li>
 *   <li>PENDING → PENDING (SQS 발행 실패, 재시도 대기)</li>
 * </ul>
 *
 * <p>Domain Event 발행:
 * <ul>
 *   <li>Application Layer에서 ApplicationEventPublisher를 통해 Event 발행</li>
 *   <li>Domain Layer는 순수 비즈니스 로직만 포함 (Framework 독립적)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class TaskMessageOutbox {

    private final Long outboxId;
    private final TaskId taskId;
    private final TaskType taskType;
    private TaskMessageStatus status;
    private int retryCount;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime sentAt;

    private TaskMessageOutbox(
        Long outboxId,
        TaskId taskId,
        TaskType taskType,
        TaskMessageStatus status,
        int retryCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime sentAt
    ) {
        this.outboxId = outboxId;
        this.taskId = Objects.requireNonNull(taskId, "taskId는 필수입니다");
        this.taskType = Objects.requireNonNull(taskType, "taskType은 필수입니다");
        this.status = Objects.requireNonNull(status, "status는 필수입니다");
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt는 필수입니다");
        this.sentAt = sentAt;
    }

    /**
     * 새로운 Outbox 메시지 생성 (PENDING 상태)
     *
     * <p>Note: Domain Event 발행은 Application Layer에서 처리
     */
    public static TaskMessageOutbox createPending(TaskId taskId, TaskType taskType) {
        return new TaskMessageOutbox(
            null,
            taskId,
            taskType,
            TaskMessageStatus.PENDING,
            0,
            null,
            LocalDateTime.now(),
            null
        );
    }

    /**
     * 재구성용 팩토리 메서드 (Persistence → Domain)
     */
    public static TaskMessageOutbox reconstitute(
        Long outboxId,
        TaskId taskId,
        TaskType taskType,
        TaskMessageStatus status,
        int retryCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime sentAt
    ) {
        return new TaskMessageOutbox(
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

    /**
     * SQS 발행 성공 처리
     */
    public void markSent() {
        if (this.status == TaskMessageStatus.SENT) {
            throw new IllegalStateException("이미 발행된 메시지입니다");
        }
        this.status = TaskMessageStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * SQS 발행 실패 처리 (재시도 카운트 증가)
     */
    public void markFailed(String errorMessage) {
        if (this.status == TaskMessageStatus.SENT) {
            throw new IllegalStateException("이미 발행된 메시지입니다");
        }
        this.retryCount++;
        this.errorMessage = errorMessage;
    }

    /**
     * 재시도 가능 여부 (최대 3회)
     */
    public boolean canRetry() {
        return this.status == TaskMessageStatus.PENDING && this.retryCount < 3;
    }

    /**
     * TaskMessageCreatedEvent 생성
     *
     * <p>PENDING 상태일 때만 Event 생성 가능
     * <p>Application Layer에서 이 Event를 가져와 ApplicationEventPublisher로 발행
     *
     * @return TaskMessageCreatedEvent (PENDING 상태일 때만), 아니면 null
     */
    public TaskMessageCreatedEvent createEvent() {
        if (this.status != TaskMessageStatus.PENDING) {
            return null;
        }
        return new TaskMessageCreatedEvent(this.outboxId);
    }

    // Getters
    public Long getOutboxId() {
        return outboxId;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public TaskMessageStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskMessageOutbox that)) return false;
        return Objects.equals(outboxId, that.outboxId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outboxId);
    }

    @Override
    public String toString() {
        return "TaskMessageOutbox{" +
               "outboxId=" + outboxId +
               ", taskId=" + taskId +
               ", taskType=" + taskType +
               ", status=" + status +
               ", retryCount=" + retryCount +
               ", createdAt=" + createdAt +
               ", sentAt=" + sentAt +
               '}';
    }
}
