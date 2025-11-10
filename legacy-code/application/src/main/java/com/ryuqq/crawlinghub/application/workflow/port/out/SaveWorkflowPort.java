package com.ryuqq.crawlinghub.application.workflow.port.out;

import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;

/**
 * Command Port for Workflow persistence operations (CUD)
 * Follows CQRS pattern - Write operations only
 */
public interface SaveWorkflowPort {

    /**
     * Save a workflow (create or update)
     * @param workflow the workflow to save
     * @return the saved workflow with generated ID if new
     */
    CrawlWorkflow save(CrawlWorkflow workflow);

    /**
     * Delete a workflow by ID
     * @param workflowId the workflow ID to delete
     */
    void delete(WorkflowId workflowId);

}
