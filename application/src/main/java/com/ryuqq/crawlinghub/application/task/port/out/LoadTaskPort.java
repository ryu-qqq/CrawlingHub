package com.ryuqq.crawlinghub.application.task.port.out;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.workflow.StepId;

import java.util.List;
import java.util.Optional;

/**
 * Query Port for CrawlTask
 * Defines operations for loading task data from persistence
 */
public interface LoadTaskPort {

    /**
     * Find task by ID
     * @param taskId the task ID
     * @return optional task
     */
    Optional<CrawlTask> findById(TaskId taskId);

    /**
     * Find all tasks for an execution
     * @param executionId the execution ID
     * @return list of tasks
     */
    List<CrawlTask> findByExecutionId(ExecutionId executionId);

    /**
     * Find tasks by execution ID and status
     * @param executionId the execution ID
     * @param status the task status
     * @return list of tasks matching the filters
     */
    List<CrawlTask> findByExecutionIdAndStatus(ExecutionId executionId, TaskStatus status);

    /**
     * Find tasks by execution ID and step ID
     * @param executionId the execution ID
     * @param stepId the step ID
     * @return list of tasks matching the filters
     */
    List<CrawlTask> findByExecutionIdAndStepId(ExecutionId executionId, StepId stepId);

    /**
     * Find tasks with dynamic filters
     * @param executionId the execution ID
     * @param status optional status filter
     * @param stepId optional step ID filter
     * @return list of tasks matching the filters
     */
    List<CrawlTask> findWithFilters(ExecutionId executionId, TaskStatus status, StepId stepId);
}
