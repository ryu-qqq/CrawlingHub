package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.common.StepType;

/**
 * Parameter object for WorkflowStep reconstitution from database.
 * Encapsulates reconstruction parameters to comply with architecture rules.
 */
public record WorkflowStepReconstituteParams(
        StepId stepId,
        WorkflowId workflowId,
        String stepName,
        Integer stepOrder,
        StepType stepType,
        String endpointKey,
        Boolean parallelExecution,
        String stepConfig
) {
    public static WorkflowStepReconstituteParams of(
            StepId stepId,
            WorkflowId workflowId,
            String stepName,
            Integer stepOrder,
            StepType stepType,
            String endpointKey,
            Boolean parallelExecution,
            String stepConfig
    ) {
        return new WorkflowStepReconstituteParams(
                stepId, workflowId, stepName, stepOrder,
                stepType, endpointKey, parallelExecution, stepConfig
        );
    }
}
