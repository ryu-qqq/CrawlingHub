package com.ryuqq.crawlinghub.domain.task;

public class TaskOutputData {

    private final Long outputDataId;
    private final Long taskId;
    private final String outputKey;
    private final String outputValue;
    private final String outputType;

    private TaskOutputData(Long outputDataId, Long taskId, String outputKey, String outputValue, String outputType) {
        this.outputDataId = outputDataId;
        this.taskId = taskId;
        this.outputKey = outputKey;
        this.outputValue = outputValue;
        this.outputType = outputType;
    }

    public Long getOutputDataId() {
        return outputDataId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public String getOutputValue() {
        return outputValue;
    }

    public String getOutputType() {
        return outputType;
    }

    public static TaskOutputData create(Long taskId, String outputKey, String outputValue, String outputType) {
        validateCreate(taskId, outputKey, outputValue, outputType);
        return new TaskOutputData(null, taskId, outputKey, outputValue, outputType);
    }

    public static TaskOutputData reconstitute(Long outputDataId, Long taskId, String outputKey,
                                             String outputValue, String outputType) {
        return new TaskOutputData(outputDataId, taskId, outputKey, outputValue, outputType);
    }

    private static void validateCreate(Long taskId, String outputKey, String outputValue, String outputType) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (outputKey == null || outputKey.isBlank()) {
            throw new IllegalArgumentException("Output key cannot be null or blank");
        }
        if (outputValue == null || outputValue.isBlank()) {
            throw new IllegalArgumentException("Output value cannot be null or blank");
        }
        if (outputType == null || outputType.isBlank()) {
            throw new IllegalArgumentException("Output type cannot be null or blank");
        }
    }

}
