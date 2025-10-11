package com.ryuqq.crawlinghub.application.workflow.port.out;

import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;

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
     * Find all active workflows
     * @return list of active workflows
     */
    List<CrawlWorkflow> findActiveWorkflows();

}
