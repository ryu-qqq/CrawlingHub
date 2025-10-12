package com.ryuqq.crawlinghub.adapter.in.web.schedule;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for schedule detail
 * Contains full schedule information including input parameters
 */
public record ScheduleResponse(
        Long scheduleId,
        Long workflowId,
        String scheduleName,
        String cronExpression,
        String timezone,
        boolean isEnabled,
        String eventbridgeRuleName,
        LocalDateTime nextExecutionTime,
        List<InputParamResponse> inputParams
) {
    public static ScheduleResponse from(CrawlSchedule schedule, List<ScheduleInputParam> inputParams) {
        List<InputParamResponse> paramResponses = inputParams != null
                ? inputParams.stream()
                        .map(InputParamResponse::from)
                        .toList()
                : List.of();

        return new ScheduleResponse(
                schedule.getScheduleId().value(),
                schedule.getWorkflowId().value(),
                schedule.getScheduleName(),
                schedule.getCronExpression(),
                schedule.getTimezone(),
                schedule.isEnabled(),
                schedule.getEventbridgeRuleName(),
                schedule.getNextExecutionTime(),
                paramResponses
        );
    }

    public record InputParamResponse(
            Long inputParamId,
            String paramKey,
            String paramValue,
            String paramType
    ) {
        public static InputParamResponse from(ScheduleInputParam param) {
            return new InputParamResponse(
                    param.getInputParamId(),
                    param.getParamKey(),
                    param.getParamValue(),
                    param.getParamType().name()
            );
        }
    }
}
