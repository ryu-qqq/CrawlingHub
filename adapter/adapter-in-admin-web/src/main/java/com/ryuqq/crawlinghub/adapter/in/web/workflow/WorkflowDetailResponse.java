package com.ryuqq.crawlinghub.adapter.in.web.workflow;

import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;

import java.util.List;

/**
 * Response DTO for workflow detail retrieval
 * Must be an immutable Java record (enforced by architecture tests)
 * Includes complete workflow structure with all steps, params, and outputs
 *
 * @param workflowId the workflow ID
 * @param siteId the site ID
 * @param workflowName the workflow name
 * @param workflowDescription the workflow description
 * @param isActive whether the workflow is active
 * @param steps list of workflow steps with full details
 */
public record WorkflowDetailResponse(
        Long workflowId,
        Long siteId,
        String workflowName,
        String workflowDescription,
        Boolean isActive,
        List<WorkflowStepResponse> steps
) {

    /**
     * Create WorkflowDetailResponse from domain model
     *
     * @param workflow the domain model
     * @return detail response DTO
     */
    public static WorkflowDetailResponse from(CrawlWorkflow workflow) {
        List<WorkflowStepResponse> stepResponses = workflow.getSteps().stream()
                .map(step -> new WorkflowStepResponse(
                        step.getStepId() != null ? step.getStepId().value() : null,
                        step.getStepName(),
                        step.getStepOrder(),
                        step.getStepType().name(),
                        step.getEndpointKey(),
                        step.getParallelExecution(),
                        List.of(),  // TODO: Add params when WorkflowStep domain includes params
                        List.of()   // TODO: Add outputs when WorkflowStep domain includes outputs
                ))
                .toList();

        return new WorkflowDetailResponse(
                workflow.getWorkflowId() != null ? workflow.getWorkflowId().value() : null,
                workflow.getSiteId().value(),
                workflow.getWorkflowName(),
                workflow.getWorkflowDescription(),
                workflow.isActive(),
                stepResponses
        );
    }

    /**
     * Nested response DTO for workflow step
     */
    public record WorkflowStepResponse(
            Long stepId,
            String stepName,
            Integer stepOrder,
            String stepType,
            String endpointKey,
            Boolean parallelExecution,
            List<StepParamResponse> params,
            List<StepOutputResponse> outputs
    ) {}

    /**
     * Nested response DTO for step parameter
     */
    public record StepParamResponse(
            Long stepParamId,
            String paramKey,
            String paramValueExpression,
            String paramType,
            Boolean isRequired
    ) {}

    /**
     * Nested response DTO for step output
     */
    public record StepOutputResponse(
            Long stepOutputId,
            String outputKey,
            String outputPathExpression,
            String outputType
    ) {}
}
