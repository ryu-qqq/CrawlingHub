package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.common.StepType;

import java.util.List;

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
        String stepConfig,
        List<StepParam> params,
        List<StepOutput> outputs
) {
    public static WorkflowStepReconstituteParams of(
            StepId stepId,
            WorkflowId workflowId,
            String stepName,
            Integer stepOrder,
            StepType stepType,
            String endpointKey,
            Boolean parallelExecution,
            String stepConfig,
            List<StepParam> params,
            List<StepOutput> outputs
    ) {
        return new WorkflowStepReconstituteParams(
                stepId, workflowId, stepName, stepOrder,
                stepType, endpointKey, parallelExecution, stepConfig,
                params, outputs
        );
    }
}
