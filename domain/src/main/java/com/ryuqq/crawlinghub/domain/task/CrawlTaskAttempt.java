package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;

import java.time.LocalDateTime;

public class CrawlTaskAttempt {

    private final AttemptId attemptId;
    private final TaskId taskId;
    private final Integer attemptNumber;
    private TaskStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private String errorType;
    private final Long apiResponseTimeMs;

    private CrawlTaskAttempt(AttemptId attemptId, TaskId taskId, Integer attemptNumber, TaskStatus status,
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

    public AttemptId getAttemptId() {
        return attemptId;
    }

    public TaskId getTaskId() {
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

    public static CrawlTaskAttempt create(TaskId taskId, Integer attemptNumber) {
        validateCreate(taskId, attemptNumber);
        return new CrawlTaskAttempt(null, taskId, attemptNumber, TaskStatus.PENDING,
                null, null, null, null, null);
    }

    public static CrawlTaskAttempt reconstitute(AttemptId attemptId, TaskId taskId, Integer attemptNumber,
                                               TaskStatus status, LocalDateTime startedAt,
                                               LocalDateTime completedAt, String errorMessage,
                                               String errorType, Long apiResponseTimeMs) {
        return new CrawlTaskAttempt(attemptId, taskId, attemptNumber, status, startedAt,
                completedAt, errorMessage, errorType, apiResponseTimeMs);
    }

    private static void validateCreate(TaskId taskId, Integer attemptNumber) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (attemptNumber == null || attemptNumber < 1) {
            throw new IllegalArgumentException("Attempt number must be positive");
        }
    }

}
