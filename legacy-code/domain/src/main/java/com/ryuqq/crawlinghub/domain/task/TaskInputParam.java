package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.common.ParamType;

public class TaskInputParam {

    private final Long inputParamId;
    private final Long taskId;
    private final ParamType paramType;
    private final String paramKey;
    private final String paramValue;

    private TaskInputParam(Long inputParamId, Long taskId, ParamType paramType, String paramKey, String paramValue) {
        this.inputParamId = inputParamId;
        this.taskId = taskId;
        this.paramType = paramType;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
    }

    public Long getInputParamId() {
        return inputParamId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public String getParamKey() {
        return paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public static TaskInputParam create(Long taskId, ParamType paramType, String paramKey, String paramValue) {
        validateCreate(taskId, paramType, paramKey, paramValue);
        return new TaskInputParam(null, taskId, paramType, paramKey, paramValue);
    }

    public static TaskInputParam reconstitute(Long inputParamId, Long taskId, ParamType paramType,
                                             String paramKey, String paramValue) {
        return new TaskInputParam(inputParamId, taskId, paramType, paramKey, paramValue);
    }

    private static void validateCreate(Long taskId, ParamType paramType, String paramKey, String paramValue) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (paramType == null) {
            throw new IllegalArgumentException("Parameter type cannot be null");
        }
        if (paramKey == null || paramKey.isBlank()) {
            throw new IllegalArgumentException("Parameter key cannot be null or blank");
        }
        if (paramValue == null || paramValue.isBlank()) {
            throw new IllegalArgumentException("Parameter value cannot be null or blank");
        }
    }

}
