package com.ryuqq.crawlinghub.application.workflow.port.out;

import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Query Port for Workflow read operations
 * Follows CQRS pattern - Read operations only
 * Complex queries should be implemented using QueryDSL
 */
public interface LoadWorkflowPort {

    /**
     * Find a workflow by ID
     * @param workflowId the workflow ID
     * @return Optional containing the workflow if found
     */
    Optional<CrawlWorkflow> findById(WorkflowId workflowId);

    /**
     * Find all workflows by site ID
     * @param siteId the site ID
     * @return list of workflows for the site
     */
    List<CrawlWorkflow> findBySiteId(SiteId siteId);

    /**
     * Find workflows by site ID with pagination (Offset-Based)
     * Suitable for UI pagination with page numbers (< 10,000 records)
     * @param siteId the site ID
     * @param pageable pagination parameters (page, size, sort)
     * @return page of workflows for the site
     */
    Page<CrawlWorkflow> findBySiteId(SiteId siteId, Pageable pageable);

    /**
     * Find workflows by site ID with cursor-based pagination (No-Offset)
     * Performance-optimized for large datasets (> 10,000 records)
     * @param siteId the site ID
     * @param lastWorkflowId cursor - last workflow ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of workflows after the cursor
     */
    List<CrawlWorkflow> findBySiteId(SiteId siteId, Long lastWorkflowId, int pageSize);

    /**
     * Find all active workflows
     * @return list of active workflows
     */
    List<CrawlWorkflow> findActiveWorkflows();

    /**
     * Find active workflows with pagination (Offset-Based)
     * Suitable for UI pagination with page numbers (< 10,000 records)
     * @param pageable pagination parameters (page, size, sort)
     * @return page of active workflows
     */
    Page<CrawlWorkflow> findActiveWorkflows(Pageable pageable);

    /**
     * Find active workflows with cursor-based pagination (No-Offset)
     * Performance-optimized for large datasets (> 10,000 records)
     * @param lastWorkflowId cursor - last workflow ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of active workflows after the cursor
     */
    List<CrawlWorkflow> findActiveWorkflows(Long lastWorkflowId, int pageSize);

}
