package com.ryuqq.crawlinghub.application.task.port.out;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.workflow.StepId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Query Port for CrawlTask
 * Defines operations for loading task data from persistence
 * Supports both Offset-Based and No-Offset cursor-based pagination
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
     * Find tasks for an execution with Offset-Based pagination
     * @param executionId the execution ID
     * @param pageable pagination parameters
     * @return page of tasks for the execution
     */
    Page<CrawlTask> findByExecutionId(ExecutionId executionId, Pageable pageable);

    /**
     * Find tasks for an execution with No-Offset cursor-based pagination
     * @param executionId the execution ID
     * @param lastTaskId cursor - last task ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of tasks after the cursor
     */
    List<CrawlTask> findByExecutionId(ExecutionId executionId, Long lastTaskId, int pageSize);

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

    /**
     * Find tasks with dynamic filters using Offset-Based pagination
     * @param executionId the execution ID
     * @param status optional status filter
     * @param stepId optional step ID filter
     * @param pageable pagination parameters
     * @return page of tasks matching the filters
     */
    Page<CrawlTask> findWithFilters(ExecutionId executionId, TaskStatus status, StepId stepId, Pageable pageable);

    /**
     * Find tasks with dynamic filters using No-Offset cursor-based pagination
     * @param executionId the execution ID
     * @param status optional status filter
     * @param stepId optional step ID filter
     * @param lastTaskId cursor - last task ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of tasks matching the filters after the cursor
     */
    List<CrawlTask> findWithFilters(ExecutionId executionId, TaskStatus status, StepId stepId, Long lastTaskId, int pageSize);
}
