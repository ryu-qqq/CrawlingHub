package com.ryuqq.crawlinghub.domain.execution;

public class ExecutionResultSummary {

    private final Long resultSummaryId;
    private final Long executionId;
    private final String metricKey;
    private final String metricValue;
    private final String metricType;

    private ExecutionResultSummary(Long resultSummaryId, Long executionId, String metricKey,
                                  String metricValue, String metricType) {
        this.resultSummaryId = resultSummaryId;
        this.executionId = executionId;
        this.metricKey = metricKey;
        this.metricValue = metricValue;
        this.metricType = metricType;
    }

    public Long getResultSummaryId() {
        return resultSummaryId;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public String getMetricKey() {
        return metricKey;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public String getMetricType() {
        return metricType;
    }

    public static ExecutionResultSummary create(Long executionId, String metricKey, String metricValue, String metricType) {
        validateCreate(executionId, metricKey, metricValue, metricType);
        return new ExecutionResultSummary(null, executionId, metricKey, metricValue, metricType);
    }

    public static ExecutionResultSummary reconstitute(Long resultSummaryId, Long executionId, String metricKey,
                                                     String metricValue, String metricType) {
        return new ExecutionResultSummary(resultSummaryId, executionId, metricKey, metricValue, metricType);
    }

    private static void validateCreate(Long executionId, String metricKey, String metricValue, String metricType) {
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (metricKey == null || metricKey.isBlank()) {
            throw new IllegalArgumentException("Metric key cannot be null or blank");
        }
        if (metricValue == null || metricValue.isBlank()) {
            throw new IllegalArgumentException("Metric value cannot be null or blank");
        }
        if (metricType == null || metricType.isBlank()) {
            throw new IllegalArgumentException("Metric type cannot be null or blank");
        }
    }

}
