package com.ryuqq.crawlinghub.application.workflow.usecase;

import java.util.List;

/**
 * Command object for workflow registration
 * Immutable record for CQRS Command pattern
 * Handles complex nested structure: Workflow → Steps → Params/Outputs
 *
 * @param siteId the site ID
 * @param workflowName the workflow name
 * @param workflowDescription the workflow description
 * @param steps list of workflow steps with params and outputs
 */
public record RegisterWorkflowCommand(
        Long siteId,
        String workflowName,
        String workflowDescription,
        List<WorkflowStepCommand> steps
) {

    /**
     * Nested command for workflow step
     *
     * @param stepName the step name
     * @param stepOrder the execution order
     * @param stepType the step type (API_CALL, DATA_TRANSFORM, etc.)
     * @param endpointKey the endpoint key reference
     * @param parallelExecution whether this step can execute in parallel
     * @param params list of step parameters
     * @param outputs list of step outputs
     */
    public record WorkflowStepCommand(
            String stepName,
            Integer stepOrder,
            String stepType,
            String endpointKey,
            Boolean parallelExecution,
            List<StepParamCommand> params,
            List<StepOutputCommand> outputs
    ) {}

    /**
     * Nested command for step parameter
     *
     * @param paramKey the parameter key
     * @param paramValueExpression the parameter value expression
     * @param paramType the parameter type (STATIC, OUTPUT_REF, DYNAMIC)
     * @param isRequired whether this parameter is required
     */
    public record StepParamCommand(
            String paramKey,
            String paramValueExpression,
            String paramType,
            Boolean isRequired
    ) {}

    /**
     * Nested command for step output
     *
     * @param outputKey the output key
     * @param outputPathExpression the JSONPath expression for output extraction
     * @param outputType the output type (STRING, NUMBER, ARRAY, OBJECT)
     */
    public record StepOutputCommand(
            String outputKey,
            String outputPathExpression,
            String outputType
    ) {}
}
