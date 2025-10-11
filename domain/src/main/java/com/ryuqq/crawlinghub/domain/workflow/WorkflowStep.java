package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.common.StepType;

public class WorkflowStep {

    private final StepId stepId;
    private final WorkflowId workflowId;
    private final String stepName;
    private Integer stepOrder;
    private final StepType stepType;
    private final String endpointKey;
    private final Boolean parallelExecution;
    private String stepConfig;

    private WorkflowStep(StepId stepId, WorkflowId workflowId, String stepName, Integer stepOrder, StepType stepType,
                        String endpointKey, Boolean parallelExecution, String stepConfig) {
        this.stepId = stepId;
        this.workflowId = workflowId;
        this.stepName = stepName;
        this.stepOrder = stepOrder;
        this.stepType = stepType;
        this.endpointKey = endpointKey;
        this.parallelExecution = parallelExecution;
        this.stepConfig = stepConfig;
    }

    public static WorkflowStep create(WorkflowId workflowId, String stepName, Integer stepOrder,
                                     StepType stepType, String endpointKey, Boolean parallelExecution) {
        return createWithConfig(workflowId, stepName, stepOrder, stepType, endpointKey, parallelExecution, null);
    }

    public static WorkflowStep createWithConfig(WorkflowId workflowId, String stepName, Integer stepOrder,
                                               StepType stepType, String endpointKey,
                                               Boolean parallelExecution, String stepConfig) {
        validateCreate(workflowId, stepName, stepOrder, stepType, endpointKey);
        return new WorkflowStep(null, workflowId, stepName, stepOrder, stepType,
                endpointKey, parallelExecution, stepConfig);
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
                params.stepConfig()
        );
    }

    private static void validateCreate(WorkflowId workflowId, String stepName, Integer stepOrder,
                                       StepType stepType, String endpointKey) {
        if (workflowId == null) {
            throw new IllegalArgumentException("Workflow ID cannot be null");
        }
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

}
