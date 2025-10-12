package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.application.execution.usecase.GetExecutionUseCase;
import com.ryuqq.crawlinghub.application.task.usecase.GetTaskUseCase;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Crawl Execution queries
 * Thin delegation layer following Hexagonal Architecture
 *
 * Architecture Rules:
 * - NO @Transactional (transaction is in application layer)
 * - Depends ONLY on UseCase interfaces
 * - Request/Response must be Java records
 * - NO Lombok allowed
 * - Controller methods should be thin
 */
@RestController
@RequestMapping("/api/v1/executions")
public class CrawlExecutionController {

    private final GetExecutionUseCase getExecutionUseCase;
    private final GetTaskUseCase getTaskUseCase;

    public CrawlExecutionController(GetExecutionUseCase getExecutionUseCase, GetTaskUseCase getTaskUseCase) {
        this.getExecutionUseCase = getExecutionUseCase;
        this.getTaskUseCase = getTaskUseCase;
    }

    /**
     * Get list of executions with optional filters
     *
     * GET /api/v1/executions
     * Query Parameters:
     * - scheduleId (optional): filter by schedule ID
     * - status (optional): filter by execution status
     * - startDate (optional): filter by start date (inclusive)
     * - endDate (optional): filter by end date (inclusive)
     *
     * @param scheduleId optional schedule ID filter
     * @param status optional status filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return list of execution summaries
     */
    @GetMapping
    public ResponseEntity<List<ExecutionSummaryResponse>> getExecutions(
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) ExecutionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Execute UseCase with filters
        List<CrawlExecution> executions = getExecutionUseCase.getExecutionsWithFilters(
                scheduleId, status, startDate, endDate
        );

        // Domain → Response
        // TODO: Fetch schedule name, workflow name, and statistics from appropriate services
        List<ExecutionSummaryResponse> response = executions.stream()
                .map(execution -> ExecutionSummaryResponse.from(
                        execution,
                        "Schedule Name", // TODO: Fetch from schedule service
                        "Workflow Name", // TODO: Fetch from workflow service
                        0.0, // TODO: Calculate progress
                        0, // TODO: Fetch from statistics
                        0, // TODO: Fetch from statistics
                        0  // TODO: Fetch from statistics
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get execution detail by ID
     *
     * GET /api/v1/executions/{executionId}
     *
     * @param executionId the execution ID
     * @return the execution detail with statistics
     */
    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionDetailResponse> getExecutionDetail(@PathVariable Long executionId) {
        // Execute UseCase
        CrawlExecution execution = getExecutionUseCase.getById(executionId);

        // TODO: Fetch related data (schedule name, workflow name, statistics, result summaries, S3 paths)
        ExecutionDetailResponse.ExecutionStatistics statistics = new ExecutionDetailResponse.ExecutionStatistics(
                0, 0, 0, 0, 0, 0, 0, 0, 0L, 0L
        );

        // Domain → Response
        ExecutionDetailResponse response = ExecutionDetailResponse.from(
                execution,
                "Schedule Name", // TODO: Fetch from schedule service
                "Workflow Name", // TODO: Fetch from workflow service
                statistics,
                List.of(), // TODO: Fetch result summaries
                List.of()  // TODO: Fetch S3 paths
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get execution progress
     *
     * GET /api/v1/executions/{executionId}/progress
     *
     * @param executionId the execution ID
     * @return real-time execution progress information
     */
    @GetMapping("/{executionId}/progress")
    public ResponseEntity<ExecutionProgressResponse> getExecutionProgress(@PathVariable Long executionId) {
        // Execute UseCase
        CrawlExecution execution = getExecutionUseCase.getById(executionId);

        // TODO: Calculate progress, estimated time remaining, and fetch current step info
        ExecutionProgressResponse.ProgressStatistics statistics = new ExecutionProgressResponse.ProgressStatistics(
                0, 0, 0, 0, 0
        );

        // Domain → Response
        ExecutionProgressResponse response = ExecutionProgressResponse.from(
                execution,
                0.0, // TODO: Calculate progress
                null, // TODO: Fetch current step info
                statistics,
                "0m 0s" // TODO: Calculate estimated time remaining
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks for an execution
     *
     * GET /api/v1/executions/{executionId}/tasks
     * Query Parameters:
     * - status (optional): filter by task status
     * - stepId (optional): filter by workflow step ID
     *
     * @param executionId the execution ID
     * @param status optional task status filter
     * @param stepId optional step ID filter
     * @return list of task summaries
     */
    @GetMapping("/{executionId}/tasks")
    public ResponseEntity<List<TaskSummaryResponse>> getExecutionTasks(
            @PathVariable Long executionId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long stepId) {

        // Execute UseCase with filters
        List<CrawlTask> tasks = getTaskUseCase.getTasksWithFilters(executionId, status, stepId);

        // Domain → Response
        List<TaskSummaryResponse> response = tasks.stream()
                .map(TaskSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get task detail
     *
     * GET /api/v1/executions/{executionId}/tasks/{taskId}
     *
     * @param executionId the execution ID
     * @param taskId the task ID
     * @return detailed task information
     * @throws IllegalArgumentException if task does not belong to the specified execution
     */
    @GetMapping("/{executionId}/tasks/{taskId}")
    public ResponseEntity<TaskDetailResponse> getTaskDetail(
            @PathVariable Long executionId,
            @PathVariable Long taskId) {

        // Execute UseCase with execution validation
        CrawlTask task = getTaskUseCase.getByIdAndValidateExecution(taskId, executionId);

        // TODO: Fetch input params, output data, result metadata, and attempts from appropriate services
        // Domain → Response
        TaskDetailResponse response = TaskDetailResponse.from(
                task,
                "Step Name", // TODO: Fetch from workflow service
                List.of(), // TODO: Fetch input params
                List.of(), // TODO: Fetch output data
                null, // TODO: Fetch result metadata
                List.of() // TODO: Fetch attempts
        );

        return ResponseEntity.ok(response);
    }
}
