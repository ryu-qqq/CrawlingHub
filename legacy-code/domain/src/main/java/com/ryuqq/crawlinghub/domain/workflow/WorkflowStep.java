package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.common.StepType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkflowStep {

    private final StepId stepId;
    private final WorkflowId workflowId;
    private final String stepName;
    private Integer stepOrder;
    private final StepType stepType;
    private final String endpointKey;
    private final Boolean parallelExecution;
    private String stepConfig;
    private final List<StepParam> params;
    private final List<StepOutput> outputs;

    private WorkflowStep(StepId stepId, WorkflowId workflowId, String stepName, Integer stepOrder, StepType stepType,
                        String endpointKey, Boolean parallelExecution, String stepConfig,
                        List<StepParam> params, List<StepOutput> outputs) {
        this.stepId = stepId;
        this.workflowId = workflowId;
        this.stepName = stepName;
        this.stepOrder = stepOrder;
        this.stepType = stepType;
        this.endpointKey = endpointKey;
        this.parallelExecution = parallelExecution;
        this.stepConfig = stepConfig;
        this.params = params != null ? new ArrayList<>(params) : new ArrayList<>();
        this.outputs = outputs != null ? new ArrayList<>(outputs) : new ArrayList<>();
    }

    public static WorkflowStep create(WorkflowId workflowId, String stepName, Integer stepOrder,
                                     StepType stepType, String endpointKey, Boolean parallelExecution,
                                     List<StepParam> params, List<StepOutput> outputs) {
        return createWithConfig(workflowId, stepName, stepOrder, stepType, endpointKey, parallelExecution, null, params, outputs);
    }

    public static WorkflowStep createWithConfig(WorkflowId workflowId, String stepName, Integer stepOrder,
                                               StepType stepType, String endpointKey,
                                               Boolean parallelExecution, String stepConfig,
                                               List<StepParam> params, List<StepOutput> outputs) {
        validateCreate(workflowId, stepName, stepOrder, stepType, endpointKey);
        return new WorkflowStep(null, workflowId, stepName, stepOrder, stepType,
                endpointKey, parallelExecution, stepConfig, params, outputs);
    }

    public static WorkflowStep reconstitute(WorkflowStepReconstituteParams params) {
        return new WorkflowStep(
                params.stepId(),
                params.workflowId(),
                params.stepName(),
                params.stepOrder(),
                params.stepType(),
                params.endpointKey(),
                params.parallelExecution(),
                params.stepConfig(),
                params.params(),
                params.outputs()
        );
    }

    private static void validateCreate(WorkflowId workflowId, String stepName, Integer stepOrder,
                                       StepType stepType, String endpointKey) {
        // Note: workflowId can be null for newly created steps before parent workflow is persisted
        // It will be set when the workflow aggregate is saved
        if (stepName == null || stepName.isBlank()) {
            throw new IllegalArgumentException("Step name cannot be null or blank");
        }
        if (stepOrder == null || stepOrder < 0) {
            throw new IllegalArgumentException("Step order must be non-negative");
        }
        if (stepType == null) {
            throw new IllegalArgumentException("Step type cannot be null");
        }
        if (endpointKey == null || endpointKey.isBlank()) {
            throw new IllegalArgumentException("Endpoint key cannot be null or blank");
        }
    }

    public void updateStepOrder(Integer newOrder) {
        if (newOrder == null || newOrder < 0) {
            throw new IllegalArgumentException("Step order must be non-negative");
        }
        this.stepOrder = newOrder;
    }

    public void updateStepConfig(String newConfig) {
        this.stepConfig = newConfig;
    }

    public StepId getStepId() {
        return stepId;
    }

    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    public String getStepName() {
        return stepName;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public StepType getStepType() {
        return stepType;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public Boolean getParallelExecution() {
        return parallelExecution;
    }

    public String getStepConfig() {
        return stepConfig;
    }

    public List<StepParam> getParams() {
        return Collections.unmodifiableList(params);
    }

    public List<StepOutput> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

}
