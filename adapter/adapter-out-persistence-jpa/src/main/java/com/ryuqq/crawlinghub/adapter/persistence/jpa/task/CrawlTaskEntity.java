package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_task")
public class CrawlTaskEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "execution_id", nullable = false)
    private Long executionId;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "parent_task_id")
    private Long parentTaskId;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TaskStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retry_count", nullable = false)
    private Integer maxRetryCount;

    @Column(name = "queued_at")
    private LocalDateTime queuedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected CrawlTaskEntity() {
    }

    private CrawlTaskEntity(Long taskId, Long executionId, Long stepId, Long parentTaskId, String taskName,
                     TaskStatus status, Integer retryCount, Integer maxRetryCount, LocalDateTime queuedAt,
                     LocalDateTime startedAt, LocalDateTime completedAt, String errorMessage) {
        this.taskId = taskId;
        this.executionId = executionId;
        this.stepId = stepId;
        this.parentTaskId = parentTaskId;
        this.taskName = taskName;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.queuedAt = queuedAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public Long getStepId() {
        return stepId;
    }

    public Long getParentTaskId() {
        return parentTaskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public LocalDateTime getQueuedAt() {
        return queuedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long taskId;
        private Long executionId;
        private Long stepId;
        private Long parentTaskId;
        private String taskName;
        private TaskStatus status;
        private Integer retryCount;
        private Integer maxRetryCount;
        private LocalDateTime queuedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;

        public Builder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder executionId(Long executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder parentTaskId(Long parentTaskId) {
            this.parentTaskId = parentTaskId;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder maxRetryCount(Integer maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public Builder queuedAt(LocalDateTime queuedAt) {
            this.queuedAt = queuedAt;
            return this;
        }

        public Builder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public CrawlTaskEntity build() {
            return new CrawlTaskEntity(taskId, executionId, stepId, parentTaskId, taskName, status,
                                retryCount, maxRetryCount, queuedAt, startedAt, completedAt, errorMessage);
        }
    }

}
