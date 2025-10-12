package com.ryuqq.crawlinghub.application.workflow.usecase;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case for workflow retrieval
 * Implements CQRS Query pattern - Read operation
 * Supports both offset-based and cursor-based pagination
 */
@Service
@Transactional(readOnly = true)
public class GetWorkflowUseCase {

    private final LoadWorkflowPort loadWorkflowPort;

    public GetWorkflowUseCase(LoadWorkflowPort loadWorkflowPort) {
        this.loadWorkflowPort = loadWorkflowPort;
    }

    /**
     * Get workflow detail by ID
     * Returns complete workflow with all steps, params, and outputs
     *
     * @param workflowId the workflow ID
     * @return the workflow detail
     * @throws WorkflowNotFoundException if workflow not found
     */
    public CrawlWorkflow getDetail(WorkflowId workflowId) {
        return loadWorkflowPort.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + workflowId.value()));
    }

    /**
     * Get all active workflows with offset-based pagination
     * Suitable for UI pagination with page numbers (< 10,000 records)
     *
     * @param pageable pagination parameters
     * @return page of active workflows
     */
    public Page<CrawlWorkflow> getActiveWorkflows(Pageable pageable) {
        return loadWorkflowPort.findActiveWorkflows(pageable);
    }

    /**
     * Get all active workflows (no pagination)
     * Use with caution for large datasets
     *
     * @return list of all active workflows
     */
    public List<CrawlWorkflow> getAllActiveWorkflows() {
        return loadWorkflowPort.findActiveWorkflows();
    }

    /**
     * Get workflows by site ID with offset-based pagination
     *
     * @param siteId the site ID
     * @param pageable pagination parameters
     * @return page of workflows for the site
     */
    public Page<CrawlWorkflow> getWorkflowsBySite(SiteId siteId, Pageable pageable) {
        return loadWorkflowPort.findBySiteId(siteId, pageable);
    }

    /**
     * Get workflows by site ID (no pagination)
     *
     * @param siteId the site ID
     * @return list of workflows for the site
     */
    public List<CrawlWorkflow> getWorkflowsBySite(SiteId siteId) {
        return loadWorkflowPort.findBySiteId(siteId);
    }

    /**
     * Get active workflows with cursor-based pagination (No-Offset)
     * Performance-optimized for large datasets (> 10,000 records)
     *
     * @param lastWorkflowId cursor - last workflow ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of workflows after the cursor
     */
    public List<CrawlWorkflow> getActiveWorkflowsWithCursor(Long lastWorkflowId, int pageSize) {
        return loadWorkflowPort.findActiveWorkflows(lastWorkflowId, pageSize);
    }
}
