package com.ryuqq.crawlinghub.adapter.out.eventbridge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.config.EventBridgeClientProperties;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.exception.EventBridgePublishException;
import com.ryuqq.crawlinghub.application.task.dto.messaging.EventBridgeTriggerPayload;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.scheduler.model.ActionAfterCompletion;
import software.amazon.awssdk.services.scheduler.model.CreateScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindow;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindowMode;
import software.amazon.awssdk.services.scheduler.model.ScheduleState;
import software.amazon.awssdk.services.scheduler.model.Target;
import software.amazon.awssdk.services.scheduler.model.UpdateScheduleRequest;

/**
 * EventBridge 스케줄 요청 변환 Mapper
 *
 * <p>CrawlSchedulerOutBox를 EventBridge Scheduler API 요청 객체로 변환합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class EventBridgeScheduleMapper {

    private final EventBridgeClientProperties properties;
    private final ObjectMapper objectMapper;

    public EventBridgeScheduleMapper(
            EventBridgeClientProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String toScheduleName(Long schedulerId) {
        return properties.getScheduleNamePrefix() + schedulerId;
    }

    public String toCronExpression(String cronExpression) {
        if (cronExpression.startsWith("cron(") || cronExpression.startsWith("rate(")) {
            return cronExpression;
        }
        return "cron(" + cronExpression + ")";
    }

    public Target toTarget(Long schedulerId, Long sellerId, String schedulerName) {
        String inputPayload = buildInputPayload(schedulerId, sellerId, schedulerName);
        return Target.builder()
                .arn(properties.getTargetArn())
                .roleArn(properties.getRoleArn())
                .input(inputPayload)
                .build();
    }

    public CreateScheduleRequest toCreateRequest(
            String scheduleName,
            String cronExpression,
            Target target,
            CrawlSchedulerOutBox outBox) {
        return CreateScheduleRequest.builder()
                .name(scheduleName)
                .groupName(properties.getScheduleGroupName())
                .scheduleExpression(cronExpression)
                .target(target)
                .flexibleTimeWindow(
                        FlexibleTimeWindow.builder().mode(FlexibleTimeWindowMode.OFF).build())
                .state(ScheduleState.ENABLED)
                .actionAfterCompletion(ActionAfterCompletion.NONE)
                .description(buildDescription(outBox.getSchedulerName(), outBox.getSellerId()))
                .build();
    }

    public UpdateScheduleRequest toUpdateRequest(
            String scheduleName,
            String cronExpression,
            Target target,
            ScheduleState state,
            CrawlSchedulerOutBox outBox) {
        return UpdateScheduleRequest.builder()
                .name(scheduleName)
                .groupName(properties.getScheduleGroupName())
                .scheduleExpression(cronExpression)
                .target(target)
                .flexibleTimeWindow(
                        FlexibleTimeWindow.builder().mode(FlexibleTimeWindowMode.OFF).build())
                .state(state)
                .actionAfterCompletion(ActionAfterCompletion.NONE)
                .description(buildDescription(outBox.getSchedulerName(), outBox.getSellerId()))
                .build();
    }

    private String buildInputPayload(Long schedulerId, Long sellerId, String schedulerName) {
        try {
            EventBridgeTriggerPayload payload =
                    new EventBridgeTriggerPayload(
                            schedulerId, sellerId, schedulerName, "<aws.scheduler.scheduled-time>");
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new EventBridgePublishException("페이로드 직렬화 실패", e);
        }
    }

    private String buildDescription(String schedulerName, Long sellerId) {
        return String.format("CrawlingHub Scheduler: %s (Seller: %d)", schedulerName, sellerId);
    }
}
