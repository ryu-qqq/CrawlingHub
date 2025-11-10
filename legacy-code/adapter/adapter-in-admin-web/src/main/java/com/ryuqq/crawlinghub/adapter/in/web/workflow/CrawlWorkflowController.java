package com.ryuqq.crawlinghub.adapter.in.web.workflow;

import com.ryuqq.crawlinghub.application.workflow.usecase.*;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Crawl Workflow management
 * Thin delegation layer following Hexagonal Architecture
 *
 * Architecture Rules (enforced by tests):
 * - NO @Transactional (transaction is in application layer)
 * - Depends ONLY on UseCase interfaces
 * - Request/Response must be Java records
 * - NO Lombok allowed
 * - Controller methods should be thin (max 3 parameters recommended)
 */
@RestController
@RequestMapping("/api/v1/workflows")
public class CrawlWorkflowController {

    private final RegisterWorkflowUseCase registerWorkflowUseCase;
    private final GetWorkflowUseCase getWorkflowUseCase;
    private final UpdateWorkflowUseCase updateWorkflowUseCase;
    private final DeleteWorkflowUseCase deleteWorkflowUseCase;

    public CrawlWorkflowController(
            RegisterWorkflowUseCase registerWorkflowUseCase,
            GetWorkflowUseCase getWorkflowUseCase,
            UpdateWorkflowUseCase updateWorkflowUseCase,
            DeleteWorkflowUseCase deleteWorkflowUseCase) {
        this.registerWorkflowUseCase = registerWorkflowUseCase;
        this.getWorkflowUseCase = getWorkflowUseCase;
        this.updateWorkflowUseCase = updateWorkflowUseCase;
        this.deleteWorkflowUseCase = deleteWorkflowUseCase;
    }

    /**
     * Create a new workflow with complex nested structure
     *
     * POST /api/v1/workflows
     *
     * @param request the workflow creation request
     * @return the created workflow response with Location header
     */
    @PostMapping
    public ResponseEntity<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        // Request → Command (encapsulated in DTO)
        RegisterWorkflowCommand command = request.toCommand();

        // Execute UseCase (returns created workflow directly)
        CrawlWorkflow workflow = registerWorkflowUseCase.execute(command);
        WorkflowResponse response = WorkflowResponse.from(workflow);

        // Build Location header URI for the created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{workflowId}")
                .buildAndExpand(response.workflowId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Get list of active workflows
     *
     * GET /api/v1/workflows
     * Query Parameters:
     * - siteId (optional): filter by site ID
     *
     * Note: Pagination support will be added when needed
     *
     * @param siteId optional site ID filter
     * @return list of workflow summaries
     */
    @GetMapping
    public ResponseEntity<List<WorkflowSummaryResponse>> getWorkflows(
            @RequestParam(required = false) Long siteId) {

        List<CrawlWorkflow> workflows;

        if (siteId != null) {
            // Filter by site ID
            workflows = getWorkflowUseCase.getWorkflowsBySite(SiteId.of(siteId));
        } else {
            // Get all active workflows
            workflows = getWorkflowUseCase.getAllActiveWorkflows();
        }

        // Domain → Response
        List<WorkflowSummaryResponse> response = workflows.stream()
                .map(WorkflowSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get workflow detail by ID
     * Returns complete workflow with all steps, params, and outputs
     *
     * GET /api/v1/workflows/{workflowId}
     *
     * @param workflowId the workflow ID
     * @return the workflow detail
     */
    @GetMapping("/{workflowId}")
    public ResponseEntity<WorkflowDetailResponse> getWorkflowDetail(@PathVariable Long workflowId) {
        // Execute UseCase
        CrawlWorkflow workflow = getWorkflowUseCase.getDetail(WorkflowId.of(workflowId));

        // Domain → Response
        WorkflowDetailResponse response = WorkflowDetailResponse.from(workflow);

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing workflow
     * Replaces all steps with new configuration
     *
     * PUT /api/v1/workflows/{workflowId}
     *
     * @param workflowId the workflow ID
     * @param request the update request
     * @return no content
     */
    @PutMapping("/{workflowId}")
    public ResponseEntity<Void> updateWorkflow(
            @PathVariable Long workflowId,
            @Valid @RequestBody UpdateWorkflowRequest request) {

        // Request → Command (encapsulated in DTO)
        UpdateWorkflowCommand command = request.toCommand(WorkflowId.of(workflowId));

        // Execute UseCase
        updateWorkflowUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    /**
     * Delete (deactivate) a workflow
     * Soft delete - workflow is marked as inactive
     *
     * DELETE /api/v1/workflows/{workflowId}
     *
     * @param workflowId the workflow ID
     * @return no content
     */
    @DeleteMapping("/{workflowId}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long workflowId) {
        // Execute UseCase
        deleteWorkflowUseCase.execute(WorkflowId.of(workflowId));

        return ResponseEntity.noContent().build();
    }
}
