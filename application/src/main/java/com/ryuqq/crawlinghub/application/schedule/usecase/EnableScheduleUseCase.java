package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.util.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleEnabledEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use case for enabling a crawl schedule
 * Publishes ScheduleEnabledEvent for EventBridge coordination
 */
@Service
public class EnableScheduleUseCase {

    private final CrawlScheduleCommandPort scheduleCommandPort;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public EnableScheduleUseCase(
            CrawlScheduleCommandPort scheduleCommandPort,
            ApplicationEventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.scheduleCommandPort = scheduleCommandPort;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
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
        try {
            Map<String, Object> targetData = new HashMap<>();
            targetData.put("scheduleId", schedule.getScheduleId().value());
            targetData.put("workflowId", schedule.getWorkflowId().value());
            targetData.put("scheduleName", schedule.getScheduleName());

            // Build inputParams map
            Map<String, String> paramsMap = new HashMap<>();
            if (inputParams != null && !inputParams.isEmpty()) {
                paramsMap = inputParams.stream()
                        .collect(Collectors.toMap(
                                ScheduleInputParam::getParamKey,
                                ScheduleInputParam::getParamValue
                        ));
            }
            targetData.put("inputParams", paramsMap);

            return objectMapper.writeValueAsString(targetData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize target input to JSON", e);
        }
    }
}
