package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.StepType;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_step")
public class WorkflowStepEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "step_name", nullable = false, length = 200)
    private String stepName;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 50)
    private StepType stepType;

    @Column(name = "endpoint_key", length = 100)
    private String endpointKey;

    @Column(name = "parallel_execution", nullable = false)
    private Boolean parallelExecution;

    @Column(name = "step_config", columnDefinition = "JSON")
    private String stepConfig;

    protected WorkflowStepEntity() {
    }

    private WorkflowStepEntity(Long stepId, Long workflowId, String stepName, Integer stepOrder, StepType stepType,
                        String endpointKey, Boolean parallelExecution, String stepConfig) {
        this.stepId = stepId;
        this.workflowId = workflowId;
        this.stepName = stepName;
        this.stepOrder = stepOrder;
        this.stepType = stepType;
        this.endpointKey = endpointKey;
        this.parallelExecution = parallelExecution;
        this.stepConfig = stepConfig;
    }

    public Long getStepId() {
        return stepId;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public String getStepName() {
        return stepName;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public StepType getStepType() {
        return stepType;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public Boolean getParallelExecution() {
        return parallelExecution;
    }

    public String getStepConfig() {
        return stepConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long stepId;
        private Long workflowId;
        private String stepName;
        private Integer stepOrder;
        private StepType stepType;
        private String endpointKey;
        private Boolean parallelExecution;
        private String stepConfig;

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder workflowId(Long workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public Builder stepName(String stepName) {
            this.stepName = stepName;
            return this;
        }

        public Builder stepOrder(Integer stepOrder) {
            this.stepOrder = stepOrder;
            return this;
        }

        public Builder stepType(StepType stepType) {
            this.stepType = stepType;
            return this;
        }

        public Builder endpointKey(String endpointKey) {
            this.endpointKey = endpointKey;
            return this;
        }

        public Builder parallelExecution(Boolean parallelExecution) {
            this.parallelExecution = parallelExecution;
            return this;
        }

        public Builder stepConfig(String stepConfig) {
            this.stepConfig = stepConfig;
            return this;
        }

        public WorkflowStepEntity build() {
            return new WorkflowStepEntity(stepId, workflowId, stepName, stepOrder, stepType,
                    endpointKey, parallelExecution, stepConfig);
        }
    }

}
