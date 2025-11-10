package com.ryuqq.crawlinghub.application.schedule.usecase;

import java.util.List;

/**
 * Command object for schedule registration
 * Immutable record for CQRS Command pattern
 *
 * @param workflowId the workflow ID
 * @param scheduleName the schedule name
 * @param cronExpression the cron expression (standard format)
 * @param timezone the timezone (e.g., Asia/Seoul)
 * @param inputParams list of schedule input parameters
 */
public record RegisterScheduleCommand(
        Long workflowId,
        String scheduleName,
        String cronExpression,
        String timezone,
        List<InputParamCommand> inputParams
) {

    /**
     * Nested command for input parameter
     *
     * @param paramKey the parameter key
     * @param paramValue the parameter value
     * @param paramType the parameter type (STATIC, DYNAMIC, OUTPUT_REF, etc.)
     */
    public record InputParamCommand(
            String paramKey,
            String paramValue,
            String paramType
    ) {}
}
