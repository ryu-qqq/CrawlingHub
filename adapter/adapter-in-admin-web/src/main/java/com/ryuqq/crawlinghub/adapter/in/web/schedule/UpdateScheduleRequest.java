package com.ryuqq.crawlinghub.adapter.in.web.schedule;

import com.ryuqq.crawlinghub.application.schedule.usecase.UpdateScheduleCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for updating an existing schedule
 */
public record UpdateScheduleRequest(
        @Size(max = 100, message = "Cron expression must not exceed 100 characters")
        String cronExpression,

        @Valid
        List<InputParamRequest> inputParams
) {
    public UpdateScheduleCommand toCommand(Long scheduleId) {
        List<UpdateScheduleCommand.InputParamCommand> paramCommands = inputParams != null
                ? inputParams.stream()
                        .map(p -> new UpdateScheduleCommand.InputParamCommand(
                                p.paramKey(),
                                p.paramValue(),
                                p.paramType()))
                        .toList()
                : null;

        return new UpdateScheduleCommand(
                scheduleId,
                null,  // scheduleName - not supported in update
                cronExpression,
                null,  // timezone - not supported in update
                paramCommands
        );
    }

    public record InputParamRequest(
            @Size(max = 100, message = "Parameter key must not exceed 100 characters")
            String paramKey,

            String paramValue,

            String paramType
    ) {
    }
}
