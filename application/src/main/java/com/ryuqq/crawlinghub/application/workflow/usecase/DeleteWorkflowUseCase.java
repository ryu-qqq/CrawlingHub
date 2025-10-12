package com.ryuqq.crawlinghub.application.workflow.usecase;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for workflow deletion (soft delete)
 * Implements CQRS Command pattern - Write operation
 * Transaction boundary is at Application layer
 *
 * Deletion Strategy:
 * - Soft delete by deactivating the workflow
 * - Preserves data for audit and recovery
 * - Use hard delete (via SaveWorkflowPort.delete()) only if required by compliance
 */
@Service
public class DeleteWorkflowUseCase {

    private final SaveWorkflowPort saveWorkflowPort;
    private final LoadWorkflowPort loadWorkflowPort;

    public DeleteWorkflowUseCase(SaveWorkflowPort saveWorkflowPort, LoadWorkflowPort loadWorkflowPort) {
        this.saveWorkflowPort = saveWorkflowPort;
        this.loadWorkflowPort = loadWorkflowPort;
    }

    /**
     * Delete (deactivate) a workflow
     * Soft delete implementation - workflow is marked as inactive
     *
     * @param workflowId the workflow ID to delete
     * @throws WorkflowNotFoundException if workflow not found
     */
    @Transactional
    public void execute(WorkflowId workflowId) {
        // 1. Find existing workflow
        CrawlWorkflow workflow = loadWorkflowPort.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException(
                        "Workflow not found with ID: " + workflowId.value()));

        // 2. Deactivate workflow (soft delete)
        workflow.deactivate();

        // 3. Save updated workflow
        saveWorkflowPort.save(workflow);
    }

    /**
     * Permanently delete a workflow (hard delete)
     * Use only if required by data retention policies or compliance
     *
     * @param workflowId the workflow ID to permanently delete
     * @throws WorkflowNotFoundException if workflow not found
     */
    @Transactional
    public void hardDelete(WorkflowId workflowId) {
        // 1. Verify workflow exists
        if (!loadWorkflowPort.findById(workflowId).isPresent()) {
            throw new WorkflowNotFoundException(
                    "Workflow not found with ID: " + workflowId.value());
        }

        // 2. Permanently delete
        saveWorkflowPort.delete(workflowId);
    }
}
