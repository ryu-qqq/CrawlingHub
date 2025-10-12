package com.ryuqq.crawlinghub.application.task.usecase;

import com.ryuqq.crawlinghub.application.task.port.out.LoadTaskPort;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.workflow.StepId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for retrieving task information
 * Provides various query methods for tasks
 */
@Service
@Transactional(readOnly = true)
public class GetTaskUseCase {

    private final LoadTaskPort loadTaskPort;

    public GetTaskUseCase(LoadTaskPort loadTaskPort) {
        this.loadTaskPort = loadTaskPort;
    }

    /**
     * Gets a task by ID
     *
     * @param taskId the task ID
     * @return the task
     * @throws TaskNotFoundException if task not found
     */
    public CrawlTask getById(Long taskId) {
        return loadTaskPort.findById(TaskId.of(taskId))
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    /**
     * Gets a task by ID and validates it belongs to the specified execution
     *
     * @param taskId the task ID
     * @param executionId the execution ID to validate against
     * @return the task
     * @throws TaskNotFoundException if task not found
     * @throws IllegalArgumentException if task does not belong to the specified execution
     */
    public CrawlTask getByIdAndValidateExecution(Long taskId, Long executionId) {
        CrawlTask task = loadTaskPort.findById(TaskId.of(taskId))
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!task.getExecutionId().value().equals(executionId)) {
            throw new IllegalArgumentException(
                    String.format("Task %d does not belong to execution %d", taskId, executionId)
            );
        }

        return task;
    }

    /**
     * Gets all tasks for an execution
     *
     * @param executionId the execution ID
     * @return list of tasks
     */
    public List<CrawlTask> getByExecutionId(Long executionId) {
        return loadTaskPort.findByExecutionId(ExecutionId.of(executionId));
    }

    /**
     * Gets tasks with dynamic filters
     *
     * @param executionId the execution ID
     * @param status optional task status filter
     * @param stepId optional step ID filter
     * @return list of tasks matching the filters
     */
    public List<CrawlTask> getTasksWithFilters(Long executionId, TaskStatus status, Long stepId) {
        ExecutionId executionIdValue = ExecutionId.of(executionId);
        StepId stepIdValue = stepId != null ? StepId.of(stepId) : null;
        return loadTaskPort.findWithFilters(executionIdValue, status, stepIdValue);
    }
}
