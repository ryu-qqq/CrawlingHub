package com.ryuqq.crawlinghub.application.workflow.usecase;

import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import java.util.List;

/**
 * Command object for workflow update
 * Immutable record for CQRS Command pattern
 *
 * Strategy: Replace all steps (delete existing and create new)
 * This simplifies implementation and avoids complex diff logic
 *
 * @param workflowId the workflow ID to update
 * @param workflowDescription the new workflow description
 * @param steps the new list of workflow steps (replaces all existing steps)
 */
public record UpdateWorkflowCommand(
        WorkflowId workflowId,
        String workflowDescription,
        List<RegisterWorkflowCommand.WorkflowStepCommand> steps
) {
}
