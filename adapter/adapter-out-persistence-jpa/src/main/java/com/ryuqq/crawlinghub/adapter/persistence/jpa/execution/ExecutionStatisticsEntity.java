package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "execution_statistics")
public class ExecutionStatisticsEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id")
    private Long statisticsId;

    @Column(name = "execution_id", nullable = false, unique = true)
    private Long executionId;

    @Column(name = "total_tasks", nullable = false)
    private Integer totalTasks;

    @Column(name = "completed_tasks", nullable = false)
    private Integer completedTasks;

    @Column(name = "failed_tasks", nullable = false)
    private Integer failedTasks;

    @Column(name = "pending_tasks", nullable = false)
    private Integer pendingTasks;

    @Column(name = "running_tasks", nullable = false)
    private Integer runningTasks;

    @Column(name = "total_api_calls", nullable = false)
    private Integer totalApiCalls;

    @Column(name = "successful_api_calls", nullable = false)
    private Integer successfulApiCalls;

    @Column(name = "failed_api_calls", nullable = false)
    private Integer failedApiCalls;

    @Column(name = "total_data_size_bytes")
    private Long totalDataSizeBytes;

    @Column(name = "total_items_processed")
    private Long totalItemsProcessed;

    protected ExecutionStatisticsEntity() {
    }

    private ExecutionStatisticsEntity(Long statisticsId, Long executionId, Integer totalTasks, Integer completedTasks,
                               Integer failedTasks, Integer pendingTasks, Integer runningTasks, Integer totalApiCalls,
                               Integer successfulApiCalls, Integer failedApiCalls, Long totalDataSizeBytes,
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

    public Long getStatisticsId() {
        return statisticsId;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public Integer getCompletedTasks() {
        return completedTasks;
    }

    public Integer getFailedTasks() {
        return failedTasks;
    }

    public Integer getPendingTasks() {
        return pendingTasks;
    }

    public Integer getRunningTasks() {
        return runningTasks;
    }

    public Integer getTotalApiCalls() {
        return totalApiCalls;
    }

    public Integer getSuccessfulApiCalls() {
        return successfulApiCalls;
    }

    public Integer getFailedApiCalls() {
        return failedApiCalls;
    }

    public Long getTotalDataSizeBytes() {
        return totalDataSizeBytes;
    }

    public Long getTotalItemsProcessed() {
        return totalItemsProcessed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long statisticsId;
        private Long executionId;
        private Integer totalTasks;
        private Integer completedTasks;
        private Integer failedTasks;
        private Integer pendingTasks;
        private Integer runningTasks;
        private Integer totalApiCalls;
        private Integer successfulApiCalls;
        private Integer failedApiCalls;
        private Long totalDataSizeBytes;
        private Long totalItemsProcessed;

        public Builder statisticsId(Long statisticsId) {
            this.statisticsId = statisticsId;
            return this;
        }

        public Builder executionId(Long executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder totalTasks(Integer totalTasks) {
            this.totalTasks = totalTasks;
            return this;
        }

        public Builder completedTasks(Integer completedTasks) {
            this.completedTasks = completedTasks;
            return this;
        }

        public Builder failedTasks(Integer failedTasks) {
            this.failedTasks = failedTasks;
            return this;
        }

        public Builder pendingTasks(Integer pendingTasks) {
            this.pendingTasks = pendingTasks;
            return this;
        }

        public Builder runningTasks(Integer runningTasks) {
            this.runningTasks = runningTasks;
            return this;
        }

        public Builder totalApiCalls(Integer totalApiCalls) {
            this.totalApiCalls = totalApiCalls;
            return this;
        }

        public Builder successfulApiCalls(Integer successfulApiCalls) {
            this.successfulApiCalls = successfulApiCalls;
            return this;
        }

        public Builder failedApiCalls(Integer failedApiCalls) {
            this.failedApiCalls = failedApiCalls;
            return this;
        }

        public Builder totalDataSizeBytes(Long totalDataSizeBytes) {
            this.totalDataSizeBytes = totalDataSizeBytes;
            return this;
        }

        public Builder totalItemsProcessed(Long totalItemsProcessed) {
            this.totalItemsProcessed = totalItemsProcessed;
            return this;
        }

        public ExecutionStatisticsEntity build() {
            return new ExecutionStatisticsEntity(statisticsId, executionId, totalTasks, completedTasks,
                    failedTasks, pendingTasks, runningTasks, totalApiCalls,
                    successfulApiCalls, failedApiCalls, totalDataSizeBytes, totalItemsProcessed);
        }
    }

}
