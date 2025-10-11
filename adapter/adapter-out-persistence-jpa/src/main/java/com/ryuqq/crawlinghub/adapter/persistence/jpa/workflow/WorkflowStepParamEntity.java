package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_step_param")
public class WorkflowStepParamEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_param_id")
    private Long stepParamId;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "param_key", nullable = false, length = 100)
    private String paramKey;

    @Column(name = "param_value_expression", columnDefinition = "TEXT")
    private String paramValueExpression;

    @Enumerated(EnumType.STRING)
    @Column(name = "param_type", nullable = false, length = 50)
    private ParamType paramType;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    protected WorkflowStepParamEntity() {
    }

    private WorkflowStepParamEntity(Long stepParamId, Long stepId, String paramKey, String paramValueExpression,
                             ParamType paramType, Boolean isRequired) {
        this.stepParamId = stepParamId;
        this.stepId = stepId;
        this.paramKey = paramKey;
        this.paramValueExpression = paramValueExpression;
        this.paramType = paramType;
        this.isRequired = isRequired;
    }

    public Long getStepParamId() {
        return stepParamId;
    }

    public Long getStepId() {
        return stepId;
    }

    public String getParamKey() {
        return paramKey;
    }

    public String getParamValueExpression() {
        return paramValueExpression;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void updateParamValue(String newValue) {
        this.paramValueExpression = newValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long stepParamId;
        private Long stepId;
        private String paramKey;
        private String paramValueExpression;
        private ParamType paramType;
        private Boolean isRequired;

        public Builder stepParamId(Long stepParamId) {
            this.stepParamId = stepParamId;
            return this;
        }

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder paramKey(String paramKey) {
            this.paramKey = paramKey;
            return this;
        }

        public Builder paramValueExpression(String paramValueExpression) {
            this.paramValueExpression = paramValueExpression;
            return this;
        }

        public Builder paramType(ParamType paramType) {
            this.paramType = paramType;
            return this;
        }

        public Builder isRequired(Boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }

        public WorkflowStepParamEntity build() {
            return new WorkflowStepParamEntity(stepParamId, stepId, paramKey, paramValueExpression, paramType, isRequired);
        }
    }

}
