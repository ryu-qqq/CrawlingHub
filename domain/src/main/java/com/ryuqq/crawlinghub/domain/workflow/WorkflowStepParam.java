package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.common.ParamType;

public class WorkflowStepParam {

    private final Long stepParamId;
    private final Long stepId;
    private final String paramKey;
    private String paramValueExpression;
    private final ParamType paramType;
    private final Boolean isRequired;

    private WorkflowStepParam(Long stepParamId, Long stepId, String paramKey, String paramValueExpression,
                             ParamType paramType, Boolean isRequired) {
        this.stepParamId = stepParamId;
        this.stepId = stepId;
        this.paramKey = paramKey;
        this.paramValueExpression = paramValueExpression;
        this.paramType = paramType;
        this.isRequired = isRequired;
    }

    public void updateParamValue(String newValue) {
        this.paramValueExpression = newValue;
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

    public static WorkflowStepParam create(Long stepId, String paramKey, String paramValueExpression,
                                          ParamType paramType, Boolean isRequired) {
        validateCreate(stepId, paramKey, paramValueExpression, paramType, isRequired);
        return new WorkflowStepParam(null, stepId, paramKey, paramValueExpression, paramType, isRequired);
    }

    public static WorkflowStepParam reconstitute(Long stepParamId, Long stepId, String paramKey,
                                                String paramValueExpression, ParamType paramType, Boolean isRequired) {
        return new WorkflowStepParam(stepParamId, stepId, paramKey, paramValueExpression, paramType, isRequired);
    }

    private static void validateCreate(Long stepId, String paramKey, String paramValueExpression,
                                       ParamType paramType, Boolean isRequired) {
        if (stepId == null) {
            throw new IllegalArgumentException("Step ID cannot be null");
        }
        if (paramKey == null || paramKey.isBlank()) {
            throw new IllegalArgumentException("Parameter key cannot be null or blank");
        }
        if (paramValueExpression == null || paramValueExpression.isBlank()) {
            throw new IllegalArgumentException("Parameter value expression cannot be null or blank");
        }
        if (paramType == null) {
            throw new IllegalArgumentException("Parameter type cannot be null");
        }
        if (isRequired == null) {
            throw new IllegalArgumentException("IsRequired flag cannot be null");
        }
    }

}
