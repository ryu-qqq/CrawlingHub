package com.ryuqq.crawlinghub.adapter.in.web.workflow;

import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;

/**
 * Response DTO for workflow creation operation
 * Must be an immutable Java record (enforced by architecture tests)
 *
 * @param workflowId the workflow ID
 * @param siteId the site ID
 * @param workflowName the workflow name
 * @param workflowDescription the workflow description
 * @param isActive whether the workflow is active
 */
public record WorkflowResponse(
        Long workflowId,
        Long siteId,
        String workflowName,
        String workflowDescription,
        Boolean isActive
) {

    /**
     * Create WorkflowResponse from domain model
     *
     * @param workflow the domain model
     * @return response DTO
     */
    public static WorkflowResponse from(CrawlWorkflow workflow) {
        return new WorkflowResponse(
                workflow.getWorkflowId() != null ? workflow.getWorkflowId().value() : null,
                workflow.getSiteId().value(),
                workflow.getWorkflowName(),
                workflow.getWorkflowDescription(),
                workflow.isActive()
        );
    }
}
