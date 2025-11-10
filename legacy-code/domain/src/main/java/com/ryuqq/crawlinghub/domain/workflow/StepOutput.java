package com.ryuqq.crawlinghub.domain.workflow;

/**
 * Domain model for workflow step output
 * Represents data extracted from step execution result
 */
public class StepOutput {

    private final StepOutputId outputId;
    private final StepId stepId;
    private final String outputKey;
    private final String outputPathExpression;
    private final String outputType;

    private StepOutput(StepOutputId outputId, StepId stepId, String outputKey,
                      String outputPathExpression, String outputType) {
        this.outputId = outputId;
        this.stepId = stepId;
        this.outputKey = outputKey;
        this.outputPathExpression = outputPathExpression;
        this.outputType = outputType;
    }

    public static StepOutput create(StepId stepId, String outputKey, String outputPathExpression, String outputType) {
        validateCreate(stepId, outputKey, outputPathExpression, outputType);
        return new StepOutput(null, stepId, outputKey, outputPathExpression, outputType);
    }

    public static StepOutput reconstitute(StepOutputId outputId, StepId stepId, String outputKey,
                                         String outputPathExpression, String outputType) {
        return new StepOutput(outputId, stepId, outputKey, outputPathExpression, outputType);
    }

    private static void validateCreate(StepId stepId, String outputKey, String outputPathExpression, String outputType) {
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

    public StepOutputId getOutputId() {
        return outputId;
    }

    public StepId getStepId() {
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

}
