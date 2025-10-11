package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "execution_result_summary")
public class ExecutionResultSummaryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_summary_id")
    private Long resultSummaryId;

    @Column(name = "execution_id", nullable = false)
    private Long executionId;

    @Column(name = "metric_key", nullable = false, length = 100)
    private String metricKey;

    @Column(name = "metric_value", columnDefinition = "TEXT")
    private String metricValue;

    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    protected ExecutionResultSummaryEntity() {
    }

    private ExecutionResultSummaryEntity(Long resultSummaryId, Long executionId, String metricKey,
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long resultSummaryId;
        private Long executionId;
        private String metricKey;
        private String metricValue;
        private String metricType;

        public Builder resultSummaryId(Long resultSummaryId) {
            this.resultSummaryId = resultSummaryId;
            return this;
        }

        public Builder executionId(Long executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder metricKey(String metricKey) {
            this.metricKey = metricKey;
            return this;
        }

        public Builder metricValue(String metricValue) {
            this.metricValue = metricValue;
            return this;
        }

        public Builder metricType(String metricType) {
            this.metricType = metricType;
            return this;
        }

        public ExecutionResultSummaryEntity build() {
            return new ExecutionResultSummaryEntity(resultSummaryId, executionId, metricKey, metricValue, metricType);
        }
    }

}
