package com.ryuqq.crawlinghub.adapter.in.web.workflow;

import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;

/**
 * Response DTO for workflow list operations (summary view)
 * Must be an immutable Java record (enforced by architecture tests)
 * Lightweight version without full step details
 *
 * @param workflowId the workflow ID
 * @param siteId the site ID
 * @param workflowName the workflow name
 * @param isActive whether the workflow is active
 * @param stepCount number of steps in the workflow
 */
public record WorkflowSummaryResponse(
        Long workflowId,
        Long siteId,
        String workflowName,
        Boolean isActive,
        Integer stepCount
) {

    /**
     * Create WorkflowSummaryResponse from domain model
     *
     * @param workflow the domain model
     * @return summary response DTO
     */
    public static WorkflowSummaryResponse from(CrawlWorkflow workflow) {
        return new WorkflowSummaryResponse(
                workflow.getWorkflowId() != null ? workflow.getWorkflowId().value() : null,
                workflow.getSiteId().value(),
                workflow.getWorkflowName(),
                workflow.isActive(),
                0  // TODO: Get step count from workflow when domain model supports it
        );
    }
}
