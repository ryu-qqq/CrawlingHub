package com.ryuqq.crawlinghub.domain.execution;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;

import java.time.LocalDateTime;

public class CrawlExecution {

    private final ExecutionId executionId;
    private final ScheduleId scheduleId;
    private final String executionName;
    private ExecutionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;

    private CrawlExecution(ExecutionId executionId, ScheduleId scheduleId, String executionName, ExecutionStatus status,
                          LocalDateTime startedAt, LocalDateTime completedAt, String errorMessage) {
        this.executionId = executionId;
        this.scheduleId = scheduleId;
        this.executionName = executionName;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
    }

    public static CrawlExecution create(ScheduleId scheduleId, String executionName) {
        validateCreate(scheduleId, executionName);
        return new CrawlExecution(null, scheduleId, executionName, ExecutionStatus.PENDING, null, null, null);
    }

    public static CrawlExecution reconstitute(ExecutionId executionId, ScheduleId scheduleId, String executionName,
                                             ExecutionStatus status, LocalDateTime startedAt,
                                             LocalDateTime completedAt, String errorMessage) {
        return new CrawlExecution(executionId, scheduleId, executionName, status,
                startedAt, completedAt, errorMessage);
    }

    private static void validateCreate(ScheduleId scheduleId, String executionName) {
        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (executionName == null || executionName.isBlank()) {
            throw new IllegalArgumentException("Execution name cannot be null or blank");
        }
    }

    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    public ExecutionId getExecutionId() {
        return executionId;
    }

    public ScheduleId getScheduleId() {
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

}
