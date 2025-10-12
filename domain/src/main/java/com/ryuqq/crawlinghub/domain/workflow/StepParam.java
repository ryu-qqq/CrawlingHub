package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.common.ParamType;

/**
 * Domain model for workflow step parameter
 * Represents a parameter that will be passed to the step execution
 */
public class StepParam {

    private final StepParamId paramId;
    private final StepId stepId;
    private final String paramKey;
    private final String paramValueExpression;
    private final ParamType paramType;
    private final Boolean isRequired;

    private StepParam(StepParamId paramId, StepId stepId, String paramKey, String paramValueExpression,
                     ParamType paramType, Boolean isRequired) {
        this.paramId = paramId;
        this.stepId = stepId;
        this.paramKey = paramKey;
        this.paramValueExpression = paramValueExpression;
        this.paramType = paramType;
        this.isRequired = isRequired;
    }

    public static StepParam create(StepId stepId, String paramKey, String paramValueExpression,
                                  ParamType paramType, Boolean isRequired) {
        validateCreate(stepId, paramKey, paramValueExpression, paramType, isRequired);
        return new StepParam(null, stepId, paramKey, paramValueExpression, paramType, isRequired);
    }

    public static StepParam reconstitute(StepParamId paramId, StepId stepId, String paramKey,
                                        String paramValueExpression, ParamType paramType, Boolean isRequired) {
        return new StepParam(paramId, stepId, paramKey, paramValueExpression, paramType, isRequired);
    }

    private static void validateCreate(StepId stepId, String paramKey, String paramValueExpression,
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

    public StepParamId getParamId() {
        return paramId;
    }

    public StepId getStepId() {
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

}
