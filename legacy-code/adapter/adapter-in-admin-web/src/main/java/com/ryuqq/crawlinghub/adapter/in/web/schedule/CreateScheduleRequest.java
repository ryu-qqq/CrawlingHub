package com.ryuqq.crawlinghub.adapter.in.web.schedule;

import com.ryuqq.crawlinghub.application.schedule.usecase.RegisterScheduleCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for creating a new crawl schedule
 */
public record CreateScheduleRequest(
        @NotNull(message = "Workflow ID is required")
        Long workflowId,

        @NotBlank(message = "Schedule name is required")
        @Size(max = 200, message = "Schedule name must not exceed 200 characters")
        String scheduleName,

        @NotBlank(message = "Cron expression is required")
        @Size(max = 100, message = "Cron expression must not exceed 100 characters")
        String cronExpression,

        @NotBlank(message = "Timezone is required")
        @Size(max = 50, message = "Timezone must not exceed 50 characters")
        String timezone,

        @Valid
        List<InputParamRequest> inputParams
) {
    public RegisterScheduleCommand toCommand() {
        List<RegisterScheduleCommand.InputParamCommand> paramCommands = inputParams != null
                ? inputParams.stream()
                        .map(p -> new RegisterScheduleCommand.InputParamCommand(
                                p.paramKey(),
                                p.paramValue(),
                                p.paramType()))
                        .toList()
                : List.of();

        return new RegisterScheduleCommand(
                workflowId,
                scheduleName,
                cronExpression,
                timezone,
                paramCommands
        );
    }

    public record InputParamRequest(
            @NotBlank(message = "Parameter key is required")
            @Size(max = 100, message = "Parameter key must not exceed 100 characters")
            String paramKey,

            String paramValue,

            @NotBlank(message = "Parameter type is required")
            String paramType
    ) {
    }
}
