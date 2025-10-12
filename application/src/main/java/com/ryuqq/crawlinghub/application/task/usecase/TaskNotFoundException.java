package com.ryuqq.crawlinghub.application.task.usecase;

/**
 * Exception thrown when a task is not found
 */
public class TaskNotFoundException extends RuntimeException {

    private final Long taskId;

    public TaskNotFoundException(Long taskId) {
        super("Task not found with ID: " + taskId);
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }
}
