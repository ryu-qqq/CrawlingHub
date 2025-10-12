package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.util.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleEnabledEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for enabling a crawl schedule
 * Publishes ScheduleEnabledEvent for EventBridge coordination
 */
@Service
public class EnableScheduleUseCase {

    private final CrawlScheduleCommandPort scheduleCommandPort;
    private final ApplicationEventPublisher eventPublisher;

    public EnableScheduleUseCase(
            CrawlScheduleCommandPort scheduleCommandPort,
            ApplicationEventPublisher eventPublisher) {
        this.scheduleCommandPort = scheduleCommandPort;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Enables a schedule by updating database and publishing event
     * EventBridge coordination happens in event handler after transaction commit
     *
     * @param scheduleId the schedule ID to enable
     * @throws ScheduleNotFoundException if schedule not found
     * @throws InvalidScheduleException if schedule cannot be enabled
     */
    @Transactional
    public void execute(Long scheduleId) {
        // 1. Find existing schedule
        CrawlSchedule schedule = scheduleCommandPort.findById(ScheduleId.of(scheduleId))
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        // 2. Check if already enabled
        if (schedule.isEnabled()) {
            throw new InvalidScheduleException("Schedule is already enabled: " + scheduleId);
        }

        // 3. Update schedule status in database
        schedule.enable();
        scheduleCommandPort.save(schedule);

        // 4. Prepare data for EventBridge coordination
        String awsCronExpression = CronExpressionValidator.convertToAwsCronExpression(
                schedule.getCronExpression()
        );
        List<ScheduleInputParam> inputParams = scheduleCommandPort.findInputParamsByScheduleId(scheduleId);
        String targetInput = buildTargetInput(schedule, inputParams);

        // 5. Publish event (EventBridge operations will happen after transaction commits)
        eventPublisher.publishEvent(new ScheduleEnabledEvent(
                scheduleId,
                schedule.getEventbridgeRuleName(),
                awsCronExpression,
                schedule.getScheduleName(),
                targetInput
        ));
    }

    private String buildTargetInput(CrawlSchedule schedule, List<ScheduleInputParam> inputParams) {
        // Build simple JSON format manually without Jackson dependency
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"scheduleId\":").append(schedule.getScheduleId().value()).append(",");
        json.append("\"workflowId\":").append(schedule.getWorkflowId().value()).append(",");
        json.append("\"scheduleName\":\"").append(escapeJson(schedule.getScheduleName())).append("\",");
        json.append("\"inputParams\":{");

        if (inputParams != null && !inputParams.isEmpty()) {
            for (int i = 0; i < inputParams.size(); i++) {
                ScheduleInputParam param = inputParams.get(i);
                json.append("\"").append(escapeJson(param.getParamKey())).append("\":");
                json.append("\"").append(escapeJson(param.getParamValue())).append("\"");
                if (i < inputParams.size() - 1) {
                    json.append(",");
                }
            }
        }

        json.append("}");
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
