package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_task_attempt")
public class CrawlTaskAttemptEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TaskStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_type", length = 100)
    private String errorType;

    @Column(name = "api_response_time_ms")
    private Long apiResponseTimeMs;

    protected CrawlTaskAttemptEntity() {
    }

    private CrawlTaskAttemptEntity(Long attemptId, Long taskId, Integer attemptNumber, TaskStatus status,
                            LocalDateTime startedAt, LocalDateTime completedAt, String errorMessage,
                            String errorType, Long apiResponseTimeMs) {
        this.attemptId = attemptId;
        this.taskId = taskId;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.apiResponseTimeMs = apiResponseTimeMs;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public TaskStatus getStatus() {
        return status;
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

    public String getErrorType() {
        return errorType;
    }

    public Long getApiResponseTimeMs() {
        return apiResponseTimeMs;
    }

    public void start() {
        this.status = TaskStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage, String errorType) {
        this.status = TaskStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long attemptId;
        private Long taskId;
        private Integer attemptNumber;
        private TaskStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
        private String errorType;
        private Long apiResponseTimeMs;

        public Builder attemptId(Long attemptId) {
            this.attemptId = attemptId;
            return this;
        }

        public Builder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder attemptNumber(Integer attemptNumber) {
            this.attemptNumber = attemptNumber;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
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

        public Builder errorType(String errorType) {
            this.errorType = errorType;
            return this;
        }

        public Builder apiResponseTimeMs(Long apiResponseTimeMs) {
            this.apiResponseTimeMs = apiResponseTimeMs;
            return this;
        }

        public CrawlTaskAttemptEntity build() {
            return new CrawlTaskAttemptEntity(attemptId, taskId, attemptNumber, status, startedAt,
                                       completedAt, errorMessage, errorType, apiResponseTimeMs);
        }
    }

}
