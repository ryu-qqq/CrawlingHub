package com.ryuqq.crawlinghub.application.workflow.usecase;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.domain.common.StepType;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowStep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        // 3. Replace steps if provided
        if (command.steps() != null && !command.steps().isEmpty()) {
            List<WorkflowStep> newSteps = command.steps().stream()
                    .map(stepCommand -> WorkflowStep.create(
                            command.workflowId(),
                            stepCommand.stepName(),
                            stepCommand.stepOrder(),
                            StepType.valueOf(stepCommand.stepType()),
                            stepCommand.endpointKey(),
                            stepCommand.parallelExecution()
                    ))
                    .toList();

            workflow.replaceSteps(newSteps);
        }

        // 4. Save updated workflow (adapter layer will handle replacing steps in DB)
        saveWorkflowPort.save(workflow);
    }
}
