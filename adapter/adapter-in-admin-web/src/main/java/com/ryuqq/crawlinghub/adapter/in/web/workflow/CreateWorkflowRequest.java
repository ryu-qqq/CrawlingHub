package com.ryuqq.crawlinghub.adapter.in.web.workflow;

import com.ryuqq.crawlinghub.application.workflow.usecase.RegisterWorkflowCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Request DTO for creating a new workflow
 * Must be an immutable Java record (enforced by architecture tests)
 * Handles complex nested structure: Workflow → Steps → Params/Outputs
 *
 * @param siteId the site ID (required, positive)
 * @param workflowName the workflow name (required, not blank)
 * @param workflowDescription the workflow description (optional)
 * @param steps list of workflow steps (required, not empty)
 */
public record CreateWorkflowRequest(

        @NotNull(message = "Site ID is required")
        @Positive(message = "Site ID must be positive")
        Long siteId,

        @NotBlank(message = "Workflow name is required")
        String workflowName,

        String workflowDescription,

        @NotEmpty(message = "워크플로우는 최소 하나 이상의 스텝을 포함해야 합니다.")
        @Valid
        List<WorkflowStepRequest> steps

) {

    /**
     * Convert request DTO to command object
     *
     * @return register workflow command
     */
    public RegisterWorkflowCommand toCommand() {
        return new RegisterWorkflowCommand(
                siteId,
                workflowName,
                workflowDescription,
                steps != null ? steps.stream()
                        .map(WorkflowStepRequest::toCommand)
                        .collect(Collectors.toList()) : List.of()
        );
    }

    /**
     * Nested request DTO for workflow step
     */
    public record WorkflowStepRequest(

            @NotBlank(message = "Step name is required")
            String stepName,

            @NotNull(message = "Step order is required")
            @Positive(message = "Step order must be positive")
            Integer stepOrder,

            @NotBlank(message = "Step type is required")
            String stepType,

            @NotBlank(message = "Endpoint key is required")
            String endpointKey,

            Boolean parallelExecution,

            @Valid
            List<StepParamRequest> params,

            @Valid
            List<StepOutputRequest> outputs

    ) {
        public RegisterWorkflowCommand.WorkflowStepCommand toCommand() {
            return new RegisterWorkflowCommand.WorkflowStepCommand(
                    stepName,
                    stepOrder,
                    stepType,
                    endpointKey,
                    parallelExecution,
                    params != null ? params.stream().map(StepParamRequest::toCommand).collect(Collectors.toList()) : List.of(),
                    outputs != null ? outputs.stream().map(StepOutputRequest::toCommand).collect(Collectors.toList()) : List.of()
            );
        }
    }

    /**
     * Nested request DTO for step parameter
     */
    public record StepParamRequest(

            @NotBlank(message = "Parameter key is required")
            String paramKey,

            @NotBlank(message = "Parameter value expression is required")
            String paramValueExpression,

            @NotBlank(message = "Parameter type is required")
            String paramType,

            @NotNull(message = "IsRequired flag is required")
            Boolean isRequired

    ) {
        public RegisterWorkflowCommand.StepParamCommand toCommand() {
            return new RegisterWorkflowCommand.StepParamCommand(
                    paramKey,
                    paramValueExpression,
                    paramType,
                    isRequired
            );
        }
    }

    /**
     * Nested request DTO for step output
     */
    public record StepOutputRequest(

            @NotBlank(message = "Output key is required")
            String outputKey,

            @NotBlank(message = "Output path expression is required")
            String outputPathExpression,

            @NotBlank(message = "Output type is required")
            String outputType

    ) {
        public RegisterWorkflowCommand.StepOutputCommand toCommand() {
            return new RegisterWorkflowCommand.StepOutputCommand(
                    outputKey,
                    outputPathExpression,
                    outputType
            );
        }
    }
}
