package com.ryuqq.crawlinghub.domain.workflow;

public class WorkflowStepOutput {

    private final Long stepOutputId;
    private final Long stepId;
    private final String outputKey;
    private String outputPathExpression;
    private final String outputType;

    private WorkflowStepOutput(Long stepOutputId, Long stepId, String outputKey, String outputPathExpression, String outputType) {
        this.stepOutputId = stepOutputId;
        this.stepId = stepId;
        this.outputKey = outputKey;
        this.outputPathExpression = outputPathExpression;
        this.outputType = outputType;
    }

    public void updateOutputExpression(String newExpression) {
        this.outputPathExpression = newExpression;
    }

    public Long getStepOutputId() {
        return stepOutputId;
    }

    public Long getStepId() {
        return stepId;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public String getOutputPathExpression() {
        return outputPathExpression;
    }

    public String getOutputType() {
        return outputType;
    }

    public static WorkflowStepOutput create(Long stepId, String outputKey, String outputPathExpression, String outputType) {
        validateCreate(stepId, outputKey, outputPathExpression, outputType);
        return new WorkflowStepOutput(null, stepId, outputKey, outputPathExpression, outputType);
    }

    public static WorkflowStepOutput reconstitute(Long stepOutputId, Long stepId, String outputKey,
                                                 String outputPathExpression, String outputType) {
        return new WorkflowStepOutput(stepOutputId, stepId, outputKey, outputPathExpression, outputType);
    }

    private static void validateCreate(Long stepId, String outputKey, String outputPathExpression, String outputType) {
        if (stepId == null) {
            throw new IllegalArgumentException("Step ID cannot be null");
        }
        if (outputKey == null || outputKey.isBlank()) {
            throw new IllegalArgumentException("Output key cannot be null or blank");
        }
        if (outputPathExpression == null || outputPathExpression.isBlank()) {
            throw new IllegalArgumentException("Output path expression cannot be null or blank");
        }
        if (outputType == null || outputType.isBlank()) {
            throw new IllegalArgumentException("Output type cannot be null or blank");
        }
    }

}
