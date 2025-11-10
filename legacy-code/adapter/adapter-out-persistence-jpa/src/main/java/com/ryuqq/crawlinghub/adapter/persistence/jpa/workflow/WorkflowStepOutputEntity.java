package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_step_output")
public class WorkflowStepOutputEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_output_id")
    private Long stepOutputId;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "output_key", nullable = false, length = 100)
    private String outputKey;

    @Column(name = "output_path_expression", nullable = false, columnDefinition = "TEXT")
    private String outputPathExpression;

    @Column(name = "output_type", nullable = false, length = 50)
    private String outputType;

    protected WorkflowStepOutputEntity() {
    }

    private WorkflowStepOutputEntity(Long stepOutputId, Long stepId, String outputKey, String outputPathExpression, String outputType) {
        this.stepOutputId = stepOutputId;
        this.stepId = stepId;
        this.outputKey = outputKey;
        this.outputPathExpression = outputPathExpression;
        this.outputType = outputType;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long stepOutputId;
        private Long stepId;
        private String outputKey;
        private String outputPathExpression;
        private String outputType;

        public Builder stepOutputId(Long stepOutputId) {
            this.stepOutputId = stepOutputId;
            return this;
        }

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public Builder outputPathExpression(String outputPathExpression) {
            this.outputPathExpression = outputPathExpression;
            return this;
        }

        public Builder outputType(String outputType) {
            this.outputType = outputType;
            return this;
        }

        public WorkflowStepOutputEntity build() {
            return new WorkflowStepOutputEntity(stepOutputId, stepId, outputKey, outputPathExpression, outputType);
        }
    }

}
