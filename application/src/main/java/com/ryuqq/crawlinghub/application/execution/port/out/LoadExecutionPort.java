package com.ryuqq.crawlinghub.application.execution.port.out;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;

import java.util.List;
import java.util.Optional;

/**
 * Query Port for Execution read operations
 * Follows CQRS pattern - Read operations only
 * Complex queries (joins, aggregations, statistics) should be implemented using QueryDSL
 */
public interface LoadExecutionPort {

    /**
     * Find an execution by ID
     * @param executionId the execution ID
     * @return Optional containing the execution if found
     */
    Optional<CrawlExecution> findById(ExecutionId executionId);

    /**
     * Find all executions by status
     * @param status the execution status
     * @return list of executions with the given status
     */
    List<CrawlExecution> findByStatus(ExecutionStatus status);

}
