package com.ryuqq.crawlinghub.domain.schedule;

import com.ryuqq.crawlinghub.domain.common.ParamType;

public class ScheduleInputParam {

    private final Long inputParamId;
    private final Long scheduleId;
    private final String paramKey;
    private final String paramValue;
    private final ParamType paramType;

    private ScheduleInputParam(Long inputParamId, Long scheduleId, String paramKey, String paramValue, ParamType paramType) {
        this.inputParamId = inputParamId;
        this.scheduleId = scheduleId;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.paramType = paramType;
    }

    public Long getInputParamId() {
        return inputParamId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public String getParamKey() {
        return paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public static ScheduleInputParam create(Long scheduleId, String paramKey, String paramValue, ParamType paramType) {
        validateCreate(scheduleId, paramKey, paramValue, paramType);
        return new ScheduleInputParam(null, scheduleId, paramKey, paramValue, paramType);
    }

    public static ScheduleInputParam reconstitute(Long inputParamId, Long scheduleId, String paramKey,
                                                 String paramValue, ParamType paramType) {
        return new ScheduleInputParam(inputParamId, scheduleId, paramKey, paramValue, paramType);
    }

    private static void validateCreate(Long scheduleId, String paramKey, String paramValue, ParamType paramType) {
        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (paramKey == null || paramKey.isBlank()) {
            throw new IllegalArgumentException("Parameter key cannot be null or blank");
        }
        if (paramValue == null || paramValue.isBlank()) {
            throw new IllegalArgumentException("Parameter value cannot be null or blank");
        }
        if (paramType == null) {
            throw new IllegalArgumentException("Parameter type cannot be null");
        }
    }

}
