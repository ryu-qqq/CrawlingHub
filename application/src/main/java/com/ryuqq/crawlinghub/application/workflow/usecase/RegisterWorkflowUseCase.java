package com.ryuqq.crawlinghub.application.workflow.usecase;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.usecase.SiteNotFoundException;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import com.ryuqq.crawlinghub.domain.common.StepType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowStep;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowStepOutput;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowStepParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Use Case for workflow registration
 * Implements CQRS Command pattern - Write operation
 * Transaction boundary is at Application layer
 *
 * Business Rules:
 * - Site must exist before creating workflow
 * - Step orders must be sequential without gaps
 * - Endpoint keys must exist in the site's endpoint configuration
 * - Output references must not create circular dependencies
 * - JSONPath expressions must be syntactically valid
 * - OUTPUT_REF parameters must reference outputs from previous steps only
 */
@Service
public class RegisterWorkflowUseCase {

    private final SaveWorkflowPort saveWorkflowPort;
    private final LoadSitePort loadSitePort;

    // JSONPath basic validation pattern
    private static final Pattern JSONPATH_PATTERN = Pattern.compile("^\\$[\\[\\.].*");
    // Output reference pattern: {{stepN.outputKey}}
    private static final Pattern OUTPUT_REF_PATTERN = Pattern.compile("^\\{\\{step(\\d+)\\.(\\w+)\\}\\}$");

    public RegisterWorkflowUseCase(SaveWorkflowPort saveWorkflowPort, LoadSitePort loadSitePort) {
        this.saveWorkflowPort = saveWorkflowPort;
        this.loadSitePort = loadSitePort;
    }

    /**
     * Register a new crawl workflow with complex nested structure
     *
     * @param command the registration command
     * @return the created workflow with generated ID
     * @throws SiteNotFoundException if site does not exist
     * @throws InvalidWorkflowException if workflow validation fails
     */
    @Transactional
    public CrawlWorkflow execute(RegisterWorkflowCommand command) {
        // 1. Validate site exists
        SiteId siteId = SiteId.of(command.siteId());
        CrawlSite site = loadSitePort.findById(siteId)
                .orElseThrow(() -> new SiteNotFoundException("Site not found with ID: " + command.siteId()));

        // 2. Create workflow domain model
        CrawlWorkflow workflow = CrawlWorkflow.create(
                siteId,
                command.workflowName(),
                command.workflowDescription()
        );

        // 3. Validate and create steps
        if (command.steps() == null || command.steps().isEmpty()) {
            throw new InvalidWorkflowException("Workflow must have at least one step");
        }

        validateStepOrders(command.steps());
        // TODO: validateEndpointKeys(command.steps(), site) - implement when site has endpoint collection
        Map<Integer, Set<String>> stepOutputMap = buildStepOutputMap(command.steps());
        validateOutputReferences(command.steps(), stepOutputMap);

        // 4. Save workflow (will be persisted with generated ID)
        // Note: In a real implementation, we need to save steps, params, and outputs together
        // This requires the adapter layer to handle the nested save operation
        return saveWorkflowPort.save(workflow);
    }

    /**
     * Validate that step orders are sequential starting from 1 without gaps
     */
    private void validateStepOrders(List<RegisterWorkflowCommand.WorkflowStepCommand> steps) {
        List<Integer> orders = steps.stream()
                .map(RegisterWorkflowCommand.WorkflowStepCommand::stepOrder)
                .sorted()
                .toList();

        for (int i = 0; i < orders.size(); i++) {
            int expected = i + 1;
            if (!orders.get(i).equals(expected)) {
                throw new InvalidWorkflowException(
                        "Step orders must be sequential starting from 1. Expected: " + expected + ", but got: " + orders.get(i)
                );
            }
        }
    }

    /**
     * Validate that all endpoint keys exist in the site's endpoint configuration
     * TODO: Implement when CrawlSite domain model includes endpoints collection
     */
    private void validateEndpointKeys(List<RegisterWorkflowCommand.WorkflowStepCommand> steps, CrawlSite site) {
        // Implementation pending: Need to fetch site endpoints from adapter layer
        // For now, skip this validation
    }

    /**
     * Build a map of step order to output keys
     * Used for validating output references
     */
    private Map<Integer, Set<String>> buildStepOutputMap(List<RegisterWorkflowCommand.WorkflowStepCommand> steps) {
        Map<Integer, Set<String>> outputMap = new HashMap<>();

        for (RegisterWorkflowCommand.WorkflowStepCommand step : steps) {
            if (step.outputs() != null && !step.outputs().isEmpty()) {
                Set<String> outputKeys = step.outputs().stream()
                        .map(RegisterWorkflowCommand.StepOutputCommand::outputKey)
                        .collect(Collectors.toSet());

                // Validate JSONPath expressions
                for (RegisterWorkflowCommand.StepOutputCommand output : step.outputs()) {
                    validateJsonPath(output.outputPathExpression(), step.stepName());
                }

                outputMap.put(step.stepOrder(), outputKeys);
            }
        }

        return outputMap;
    }

    /**
     * Validate that OUTPUT_REF parameters only reference outputs from previous steps
     * This prevents circular dependencies
     */
    private void validateOutputReferences(
            List<RegisterWorkflowCommand.WorkflowStepCommand> steps,
            Map<Integer, Set<String>> stepOutputMap) {

        for (RegisterWorkflowCommand.WorkflowStepCommand step : steps) {
            if (step.params() == null) continue;

            for (RegisterWorkflowCommand.StepParamCommand param : step.params()) {
                if ("OUTPUT_REF".equals(param.paramType())) {
                    validateOutputReference(param, step.stepOrder(), stepOutputMap, step.stepName());
                }
            }
        }
    }

    /**
     * Validate a single output reference parameter
     * Format: {{stepN.outputKey}} where N < current step order
     */
    private void validateOutputReference(
            RegisterWorkflowCommand.StepParamCommand param,
            int currentStepOrder,
            Map<Integer, Set<String>> stepOutputMap,
            String currentStepName) {

        var matcher = OUTPUT_REF_PATTERN.matcher(param.paramValueExpression());
        if (!matcher.matches()) {
            throw new InvalidWorkflowException(
                    "Invalid output reference format in step '" + currentStepName + "': " + param.paramValueExpression() +
                    ". Expected format: {{stepN.outputKey}}"
            );
        }

        int referencedStepOrder = Integer.parseInt(matcher.group(1));
        String referencedOutputKey = matcher.group(2);

        // Validate step order (must reference previous step)
        if (referencedStepOrder >= currentStepOrder) {
            throw new InvalidWorkflowException(
                    "Step '" + currentStepName + "' (order: " + currentStepOrder + ") cannot reference output from " +
                    "step " + referencedStepOrder + ". Can only reference outputs from previous steps."
            );
        }

        // Validate output key exists
        Set<String> availableOutputs = stepOutputMap.get(referencedStepOrder);
        if (availableOutputs == null || !availableOutputs.contains(referencedOutputKey)) {
            throw new InvalidWorkflowException(
                    "Output key '" + referencedOutputKey + "' not found in step " + referencedStepOrder + ". " +
                    "Available outputs: " + (availableOutputs != null ? String.join(", ", availableOutputs) : "none")
            );
        }
    }

    /**
     * Validate JSONPath expression syntax
     */
    private void validateJsonPath(String expression, String stepName) {
        if (expression == null || expression.isBlank()) {
            throw new InvalidWorkflowException("JSONPath expression cannot be empty in step: " + stepName);
        }

        if (!JSONPATH_PATTERN.matcher(expression).matches()) {
            throw new InvalidWorkflowException(
                    "Invalid JSONPath expression in step '" + stepName + "': " + expression +
                    ". Must start with '$'"
            );
        }
    }
}
