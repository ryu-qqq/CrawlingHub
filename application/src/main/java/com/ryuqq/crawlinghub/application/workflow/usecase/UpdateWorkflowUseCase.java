package com.ryuqq.crawlinghub.application.workflow.usecase;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for workflow update
 * Implements CQRS Command pattern - Write operation
 * Transaction boundary is at Application layer
 *
 * Update Strategy:
 * - Update only description and activation status
 * - Replace all steps (delete existing and create new)
 * - This simplifies implementation and avoids complex diff logic
 * - Consider versioning if you need to track changes
 */
@Service
public class UpdateWorkflowUseCase {

    private final SaveWorkflowPort saveWorkflowPort;
    private final LoadWorkflowPort loadWorkflowPort;

    public UpdateWorkflowUseCase(SaveWorkflowPort saveWorkflowPort, LoadWorkflowPort loadWorkflowPort) {
        this.saveWorkflowPort = saveWorkflowPort;
        this.loadWorkflowPort = loadWorkflowPort;
    }

    /**
     * Update an existing workflow
     *
     * @param command the update command
     * @throws WorkflowNotFoundException if workflow not found
     */
    @Transactional
    public void execute(UpdateWorkflowCommand command) {
        // 1. Find existing workflow
        CrawlWorkflow workflow = loadWorkflowPort.findById(command.workflowId())
                .orElseThrow(() -> new WorkflowNotFoundException(
                        "Workflow not found with ID: " + command.workflowId().value()));

        // 2. Update domain model
        workflow.updateDescription(command.workflowDescription());

        // 3. Save updated workflow
        // Note: The adapter layer should handle replacing steps
        // This requires deleting all existing steps and creating new ones
        saveWorkflowPort.save(workflow);
    }
}
