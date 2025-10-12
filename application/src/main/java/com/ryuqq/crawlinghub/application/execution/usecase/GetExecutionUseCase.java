package com.ryuqq.crawlinghub.application.execution.usecase;

import com.ryuqq.crawlinghub.application.execution.port.out.LoadExecutionPort;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case for retrieving execution information
 * Provides various query methods for executions
 */
@Service
@Transactional(readOnly = true)
public class GetExecutionUseCase {

    private final LoadExecutionPort loadExecutionPort;

    public GetExecutionUseCase(LoadExecutionPort loadExecutionPort) {
        this.loadExecutionPort = loadExecutionPort;
    }

    /**
     * Gets an execution by ID
     *
     * @param executionId the execution ID
     * @return the execution
     * @throws ExecutionNotFoundException if execution not found
     */
    public CrawlExecution getById(Long executionId) {
        return loadExecutionPort.findById(ExecutionId.of(executionId))
                .orElseThrow(() -> new ExecutionNotFoundException(executionId));
    }

    /**
     * Gets executions with dynamic filters
     *
     * @param scheduleId optional schedule ID filter
     * @param status optional execution status filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param pageable pagination parameters
     * @return page of executions matching the filters
     */
    public Page<CrawlExecution> getExecutionsWithFilters(
            Long scheduleId,
            ExecutionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        ScheduleId scheduleIdValue = scheduleId != null ? ScheduleId.of(scheduleId) : null;
        return loadExecutionPort.findWithFilters(scheduleIdValue, status, startDate, endDate, pageable);
    }

    /**
     * Gets all executions
     *
     * @return list of all executions
     */
    public List<CrawlExecution> getAll() {
        return loadExecutionPort.findAll();
    }

    /**
     * Gets executions by status
     *
     * @param status the execution status
     * @return list of executions with the given status
     */
    public List<CrawlExecution> getByStatus(ExecutionStatus status) {
        return loadExecutionPort.findByStatus(status);
    }

    /**
     * Gets executions by status with pagination
     *
     * @param status the execution status
     * @param pageable pagination parameters
     * @return page of executions with the given status
     */
    public Page<CrawlExecution> getByStatus(ExecutionStatus status, Pageable pageable) {
        return loadExecutionPort.findByStatus(status, pageable);
    }
}
