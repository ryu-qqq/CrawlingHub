package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_execution")
public class CrawlExecutionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "execution_name", nullable = false, length = 200)
    private String executionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ExecutionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected CrawlExecutionEntity() {
    }

    private CrawlExecutionEntity(Long executionId, Long scheduleId, String executionName, ExecutionStatus status,
                          LocalDateTime startedAt, LocalDateTime completedAt, String errorMessage) {
        this.executionId = executionId;
        this.scheduleId = scheduleId;
        this.executionName = executionName;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public String getExecutionName() {
        return executionName;
    }

    public ExecutionStatus getStatus() {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long executionId;
        private Long scheduleId;
        private String executionName;
        private ExecutionStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;

        public Builder executionId(Long executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder scheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        public Builder executionName(String executionName) {
            this.executionName = executionName;
            return this;
        }

        public Builder status(ExecutionStatus status) {
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

        public CrawlExecutionEntity build() {
            return new CrawlExecutionEntity(executionId, scheduleId, executionName, status,
                    startedAt, completedAt, errorMessage);
        }
    }

}
