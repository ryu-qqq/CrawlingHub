package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.workflow.StepId;

import java.time.LocalDateTime;

public class CrawlTask {

    private final TaskId taskId;
    private final ExecutionId executionId;
    private final StepId stepId;
    private final TaskId parentTaskId;
    private final String taskName;
    private TaskStatus status;
    private int retryCount;
    private final int maxRetryCount;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;

    private CrawlTask(TaskId taskId, ExecutionId executionId, StepId stepId, TaskId parentTaskId, String taskName,
                     TaskStatus status, int retryCount, int maxRetryCount, LocalDateTime queuedAt,
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

    public static CrawlTask create(ExecutionId executionId, StepId stepId, String taskName, int maxRetryCount) {
        return createWithParent(executionId, stepId, null, taskName, maxRetryCount);
    }

    public static CrawlTask createWithParent(ExecutionId executionId, StepId stepId, TaskId parentTaskId,
                                            String taskName, int maxRetryCount) {
        validateCreate(executionId, stepId, taskName, maxRetryCount);
        return new CrawlTask(null, executionId, stepId, parentTaskId, taskName,
                TaskStatus.PENDING, 0, maxRetryCount, null, null, null, null);
    }

    public static CrawlTask reconstitute(TaskId taskId, ExecutionId executionId, StepId stepId, TaskId parentTaskId,
                                        String taskName, TaskStatus status, int retryCount, int maxRetryCount,
                                        LocalDateTime queuedAt, LocalDateTime startedAt,
                                        LocalDateTime completedAt, String errorMessage) {
        return new CrawlTask(taskId, executionId, stepId, parentTaskId, taskName, status,
                retryCount, maxRetryCount, queuedAt, startedAt, completedAt, errorMessage);
    }

    private static void validateCreate(ExecutionId executionId, StepId stepId, String taskName, int maxRetryCount) {
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (stepId == null) {
            throw new IllegalArgumentException("Step ID cannot be null");
        }
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("Task name cannot be null or blank");
        }
        if (maxRetryCount < 0) {
            throw new IllegalArgumentException("Max retry count cannot be negative");
        }
    }

    public void enqueue() {
        if (this.status != TaskStatus.PENDING && this.status != TaskStatus.RETRY) {
            throw new IllegalStateException(
                    "Cannot enqueue task in " + status + " state. Only PENDING or RETRY tasks can be enqueued.");
        }
        this.status = TaskStatus.QUEUED;
        this.queuedAt = LocalDateTime.now();
    }

    public void start() {
        if (this.status != TaskStatus.QUEUED) {
            throw new IllegalStateException(
                    "Cannot start task in " + status + " state. Only QUEUED tasks can be started.");
        }
        this.status = TaskStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != TaskStatus.RUNNING) {
            throw new IllegalStateException(
                    "Cannot complete task in " + status + " state. Only RUNNING tasks can be completed.");
        }
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        if (this.status != TaskStatus.RUNNING) {
            throw new IllegalStateException(
                    "Cannot fail task in " + status + " state. Only RUNNING tasks can be failed.");
        }
        this.status = TaskStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }

    public void incrementRetry() {
        if (this.status != TaskStatus.FAILED) {
            throw new IllegalStateException(
                    "Cannot retry task in " + status + " state. Only FAILED tasks can be retried.");
        }
        if (!canRetry()) {
            throw new IllegalStateException("Cannot retry: maximum retry count reached");
        }
        this.retryCount++;
        this.status = TaskStatus.RETRY;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    public ExecutionId getExecutionId() {
        return executionId;
    }

    public StepId getStepId() {
        return stepId;
    }

    public TaskId getParentTaskId() {
        return parentTaskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetryCount() {
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

}
