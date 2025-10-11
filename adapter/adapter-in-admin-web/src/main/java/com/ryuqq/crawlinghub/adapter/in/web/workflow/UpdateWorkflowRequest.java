package com.ryuqq.crawlinghub.adapter.in.web.workflow;

import com.ryuqq.crawlinghub.application.workflow.usecase.UpdateWorkflowCommand;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Request DTO for updating an existing workflow
 * Must be an immutable Java record (enforced by architecture tests)
 *
 * @param workflowDescription the new workflow description (optional)
 * @param steps the new list of workflow steps (required, replaces all existing)
 */
public record UpdateWorkflowRequest(

        String workflowDescription,

        @Valid
        List<CreateWorkflowRequest.WorkflowStepRequest> steps

) {

    /**
     * Convert request DTO to command object
     *
     * @param workflowId the workflow ID to update
     * @return update workflow command
     */
    public UpdateWorkflowCommand toCommand(WorkflowId workflowId) {
        return new UpdateWorkflowCommand(
                workflowId,
                workflowDescription,
                steps != null ? steps.stream()
                        .map(CreateWorkflowRequest.WorkflowStepRequest::toCommand)
                        .collect(Collectors.toList()) : List.of()
        );
    }
}
