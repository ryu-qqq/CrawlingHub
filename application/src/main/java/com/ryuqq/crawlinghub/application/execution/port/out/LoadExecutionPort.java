package com.ryuqq.crawlinghub.application.execution.port.out;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Find executions by status with pagination (Offset-Based)
     * Suitable for UI pagination with page numbers (< 10,000 records)
     * @param status the execution status
     * @param pageable pagination parameters (page, size, sort)
     * @return page of executions with the given status
     */
    Page<CrawlExecution> findByStatus(ExecutionStatus status, Pageable pageable);

    /**
     * Find executions by status with cursor-based pagination (No-Offset)
     * Performance-optimized for large datasets (> 10,000 records)
     * @param status the execution status
     * @param lastExecutionId cursor - last execution ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of executions after the cursor
     */
    List<CrawlExecution> findByStatus(ExecutionStatus status, Long lastExecutionId, int pageSize);

}
