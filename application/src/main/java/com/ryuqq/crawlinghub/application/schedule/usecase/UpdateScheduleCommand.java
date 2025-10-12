package com.ryuqq.crawlinghub.application.schedule.usecase;

import java.util.List;

/**
 * Command object for schedule update
 * Immutable record for CQRS Command pattern
 *
 * @param scheduleId the schedule ID to update
 * @param scheduleName the schedule name (optional)
 * @param cronExpression the cron expression (optional)
 * @param timezone the timezone (optional)
 * @param inputParams list of schedule input parameters (optional)
 */
public record UpdateScheduleCommand(
        Long scheduleId,
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
     * @param paramType the parameter type
     */
    public record InputParamCommand(
            String paramKey,
            String paramValue,
            String paramType
    ) {}
}
