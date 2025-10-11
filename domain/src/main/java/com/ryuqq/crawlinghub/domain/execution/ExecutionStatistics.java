package com.ryuqq.crawlinghub.domain.execution;

public class ExecutionStatistics {

    private final StatisticsId statisticsId;
    private final ExecutionId executionId;
    private int totalTasks;
    private int completedTasks;
    private int failedTasks;
    private int pendingTasks;
    private int runningTasks;
    private int totalApiCalls;
    private int successfulApiCalls;
    private int failedApiCalls;
    private Long totalDataSizeBytes;
    private Long totalItemsProcessed;

    private ExecutionStatistics(StatisticsId statisticsId, ExecutionId executionId, int totalTasks, int completedTasks,
                               int failedTasks, int pendingTasks, int runningTasks, int totalApiCalls,
                               int successfulApiCalls, int failedApiCalls, Long totalDataSizeBytes,
                               Long totalItemsProcessed) {
        this.statisticsId = statisticsId;
        this.executionId = executionId;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.failedTasks = failedTasks;
        this.pendingTasks = pendingTasks;
        this.runningTasks = runningTasks;
        this.totalApiCalls = totalApiCalls;
        this.successfulApiCalls = successfulApiCalls;
        this.failedApiCalls = failedApiCalls;
        this.totalDataSizeBytes = totalDataSizeBytes;
        this.totalItemsProcessed = totalItemsProcessed;
    }

    public void incrementCompletedTasks() {
        this.completedTasks++;
        this.runningTasks--;
    }

    public void incrementFailedTasks() {
        this.failedTasks++;
        this.runningTasks--;
    }

    public void incrementRunningTasks() {
        this.runningTasks++;
        this.pendingTasks--;
    }

    public void recordApiCall(boolean success) {
        this.totalApiCalls++;
        if (success) {
            this.successfulApiCalls++;
        } else {
            this.failedApiCalls++;
        }
    }

    public void addDataSize(Long bytes) {
        if (this.totalDataSizeBytes == null) {
            this.totalDataSizeBytes = bytes;
        } else {
            this.totalDataSizeBytes += bytes;
        }
    }

    public void addItemsProcessed(Long count) {
        if (this.totalItemsProcessed == null) {
            this.totalItemsProcessed = count;
        } else {
            this.totalItemsProcessed += count;
        }
    }

    public StatisticsId getStatisticsId() {
        return statisticsId;
    }

    public ExecutionId getExecutionId() {
        return executionId;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public int getRunningTasks() {
        return runningTasks;
    }

    public int getTotalApiCalls() {
        return totalApiCalls;
    }

    public int getSuccessfulApiCalls() {
        return successfulApiCalls;
    }

    public int getFailedApiCalls() {
        return failedApiCalls;
    }

    public Long getTotalDataSizeBytes() {
        return totalDataSizeBytes;
    }

    public Long getTotalItemsProcessed() {
        return totalItemsProcessed;
    }

    public static ExecutionStatistics create(ExecutionId executionId, int totalTasks) {
        validateCreate(executionId, totalTasks);
        return new ExecutionStatistics(null, executionId, totalTasks, 0, 0, totalTasks, 0, 0, 0, 0, 0L, 0L);
    }

    public static ExecutionStatistics reconstitute(StatisticsId statisticsId, ExecutionId executionId, int totalTasks,
                                                  int completedTasks, int failedTasks, int pendingTasks,
                                                  int runningTasks, int totalApiCalls, int successfulApiCalls,
                                                  int failedApiCalls, Long totalDataSizeBytes, Long totalItemsProcessed) {
        return new ExecutionStatistics(statisticsId, executionId, totalTasks, completedTasks,
                failedTasks, pendingTasks, runningTasks, totalApiCalls,
                successfulApiCalls, failedApiCalls, totalDataSizeBytes, totalItemsProcessed);
    }

    private static void validateCreate(ExecutionId executionId, int totalTasks) {
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (totalTasks < 0) {
            throw new IllegalArgumentException("Total tasks cannot be negative");
        }
    }

}
