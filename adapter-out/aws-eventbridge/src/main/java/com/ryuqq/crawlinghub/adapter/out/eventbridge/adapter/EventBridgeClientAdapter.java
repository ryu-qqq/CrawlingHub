package com.ryuqq.crawlinghub.adapter.out.eventbridge.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.config.EventBridgeProperties;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerRegisteredEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerUpdatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.ActionAfterCompletion;
import software.amazon.awssdk.services.scheduler.model.CreateScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindow;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindowMode;
import software.amazon.awssdk.services.scheduler.model.ScheduleState;
import software.amazon.awssdk.services.scheduler.model.Target;
import software.amazon.awssdk.services.scheduler.model.UpdateScheduleRequest;

/**
 * AWS EventBridge Scheduler 클라이언트 어댑터
 *
 * <p><strong>용도</strong>: 스케줄러 이벤트를 AWS EventBridge Scheduler에 동기화
 *
 * <p><strong>EventBridge Scheduler API 매핑</strong>:
 *
 * <ul>
 *   <li>createScheduler → CreateSchedule API
 *   <li>updateScheduler → UpdateSchedule API (상태에 따라 Enable/Disable)
 *   <li>syncFromOutBox → OutBox 페이로드 파싱 후 적절한 API 호출
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class EventBridgeClientAdapter implements EventBridgeClientPort {

    private static final Logger log = LoggerFactory.getLogger(EventBridgeClientAdapter.class);

    private final SchedulerClient schedulerClient;
    private final EventBridgeProperties eventBridgeProperties;
    private final ObjectMapper objectMapper;

    public EventBridgeClientAdapter(
            SchedulerClient schedulerClient,
            EventBridgeProperties eventBridgeProperties,
            ObjectMapper objectMapper) {
        this.schedulerClient = schedulerClient;
        this.eventBridgeProperties = eventBridgeProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 스케줄러 등록 이벤트 동기화
     *
     * <p>AWS EventBridge Scheduler에 새 스케줄 생성
     *
     * @param event 스케줄러 등록 이벤트
     */
    @Override
    public void createScheduler(SchedulerRegisteredEvent event) {
        String scheduleName = buildScheduleName(event.getCrawlSchedulerIdValue());
        String cronExpression = convertToCronExpression(event.cronExpression().value());

        Target target = buildTarget(event);

        CreateScheduleRequest request =
                CreateScheduleRequest.builder()
                        .name(scheduleName)
                        .groupName(eventBridgeProperties.getScheduleGroupName())
                        .scheduleExpression(cronExpression)
                        .target(target)
                        .flexibleTimeWindow(
                                FlexibleTimeWindow.builder()
                                        .mode(FlexibleTimeWindowMode.OFF)
                                        .build())
                        .state(ScheduleState.ENABLED)
                        .actionAfterCompletion(ActionAfterCompletion.NONE)
                        .description(
                                buildDescription(
                                        event.getScheduleNameValue(), event.getSellerIdValue()))
                        .build();

        schedulerClient.createSchedule(request);
        log.info(
                "EventBridge 스케줄 생성 완료: name={}, sellerId={}",
                scheduleName,
                event.getSellerIdValue());
    }

    /**
     * 스케줄러 수정 이벤트 동기화
     *
     * <p>AWS EventBridge Scheduler의 스케줄 수정 또는 상태 변경
     *
     * @param event 스케줄러 수정 이벤트
     */
    @Override
    public void updateScheduler(SchedulerUpdatedEvent event) {
        String scheduleName = buildScheduleName(event.schedulerId().value());
        String cronExpression = convertToCronExpression(event.cronExpression().value());
        ScheduleState state = mapToScheduleState(event.status());

        Target target = buildTarget(event);

        UpdateScheduleRequest request =
                UpdateScheduleRequest.builder()
                        .name(scheduleName)
                        .groupName(eventBridgeProperties.getScheduleGroupName())
                        .scheduleExpression(cronExpression)
                        .target(target)
                        .flexibleTimeWindow(
                                FlexibleTimeWindow.builder()
                                        .mode(FlexibleTimeWindowMode.OFF)
                                        .build())
                        .state(state)
                        .actionAfterCompletion(ActionAfterCompletion.NONE)
                        .description(
                                buildDescription(
                                        event.schedulerName().value(), event.sellerId().value()))
                        .build();

        schedulerClient.updateSchedule(request);
        log.info("EventBridge 스케줄 수정 완료: name={}, state={}", scheduleName, state);
    }

    /**
     * 아웃박스에서 이벤트 동기화 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 아웃박스에서 이벤트 페이로드를 읽어 동기화
     *
     * @param outBox 재처리할 아웃박스
     */
    @Override
    public void syncFromOutBox(CrawlSchedulerOutBox outBox) {
        String payload = outBox.getEventPayload();

        try {
            // 페이로드에서 이벤트 타입 판별
            if (payload.contains("\"eventType\":\"REGISTERED\"")) {
                SchedulerRegisteredEvent event = parseRegisteredEvent(payload);
                createScheduler(event);
            } else if (payload.contains("\"eventType\":\"UPDATED\"")) {
                SchedulerUpdatedEvent event = parseUpdatedEvent(payload);
                updateScheduler(event);
            } else {
                log.warn("알 수 없는 이벤트 타입: outBoxId={}", outBox.getOutBoxIdValue());
            }
        } catch (Exception e) {
            log.error(
                    "OutBox 동기화 실패: outBoxId={}, error={}",
                    outBox.getOutBoxIdValue(),
                    e.getMessage());
            throw new RuntimeException("OutBox 동기화 실패", e);
        }
    }

    // ==================== Private Methods ====================

    /**
     * 스케줄 이름 생성
     *
     * @param schedulerId 스케줄러 ID
     * @return 스케줄 이름 (prefix + schedulerId)
     */
    private String buildScheduleName(Long schedulerId) {
        return eventBridgeProperties.getScheduleNamePrefix() + schedulerId;
    }

    /**
     * 크론 표현식을 EventBridge 형식으로 변환
     *
     * <p>EventBridge는 "cron()" 또는 "rate()" 형식을 사용
     *
     * @param cronExpression 크론 표현식
     * @return EventBridge 형식 표현식
     */
    private String convertToCronExpression(String cronExpression) {
        // 이미 cron() 형식이면 그대로 반환
        if (cronExpression.startsWith("cron(") || cronExpression.startsWith("rate(")) {
            return cronExpression;
        }
        // 일반 크론 표현식을 EventBridge 형식으로 변환
        return "cron(" + cronExpression + ")";
    }

    /**
     * SchedulerRegisteredEvent에서 Target 생성
     *
     * @param event 스케줄러 등록 이벤트
     * @return EventBridge Target
     */
    private Target buildTarget(SchedulerRegisteredEvent event) {
        String inputPayload =
                buildInputPayload(
                        event.getCrawlSchedulerIdValue(),
                        event.getSellerIdValue(),
                        event.getScheduleNameValue());

        return Target.builder()
                .arn(eventBridgeProperties.getTargetArn())
                .roleArn(eventBridgeProperties.getRoleArn())
                .input(inputPayload)
                .build();
    }

    /**
     * SchedulerUpdatedEvent에서 Target 생성
     *
     * @param event 스케줄러 수정 이벤트
     * @return EventBridge Target
     */
    private Target buildTarget(SchedulerUpdatedEvent event) {
        String inputPayload =
                buildInputPayload(
                        event.schedulerId().value(),
                        event.sellerId().value(),
                        event.schedulerName().value());

        return Target.builder()
                .arn(eventBridgeProperties.getTargetArn())
                .roleArn(eventBridgeProperties.getRoleArn())
                .input(inputPayload)
                .build();
    }

    /**
     * Target Input Payload 생성
     *
     * @param schedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @return JSON 형식 페이로드
     */
    private String buildInputPayload(Long schedulerId, Long sellerId, String schedulerName) {
        try {
            SchedulePayload payload = new SchedulePayload(schedulerId, sellerId, schedulerName);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("페이로드 직렬화 실패: schedulerId={}", schedulerId);
            throw new RuntimeException("페이로드 직렬화 실패", e);
        }
    }

    /**
     * 스케줄 설명 생성
     *
     * @param schedulerName 스케줄러 이름
     * @param sellerId 셀러 ID
     * @return 설명 문자열
     */
    private String buildDescription(String schedulerName, Long sellerId) {
        return String.format("CrawlingHub Scheduler: %s (Seller: %d)", schedulerName, sellerId);
    }

    /**
     * 도메인 SchedulerStatus를 EventBridge ScheduleState로 매핑
     *
     * @param status 도메인 스케줄러 상태
     * @return EventBridge 스케줄 상태
     */
    private ScheduleState mapToScheduleState(SchedulerStatus status) {
        return switch (status) {
            case ACTIVE -> ScheduleState.ENABLED;
            case INACTIVE -> ScheduleState.DISABLED;
        };
    }

    /**
     * 페이로드에서 SchedulerRegisteredEvent 파싱
     *
     * @param payload JSON 페이로드
     * @return SchedulerRegisteredEvent
     */
    private SchedulerRegisteredEvent parseRegisteredEvent(String payload) {
        try {
            return objectMapper.readValue(payload, SchedulerRegisteredEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("SchedulerRegisteredEvent 파싱 실패", e);
        }
    }

    /**
     * 페이로드에서 SchedulerUpdatedEvent 파싱
     *
     * @param payload JSON 페이로드
     * @return SchedulerUpdatedEvent
     */
    private SchedulerUpdatedEvent parseUpdatedEvent(String payload) {
        try {
            return objectMapper.readValue(payload, SchedulerUpdatedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("SchedulerUpdatedEvent 파싱 실패", e);
        }
    }

    // ==================== Inner Classes ====================

    /**
     * EventBridge Target Input Payload
     *
     * <p>Lambda 또는 다른 Target에 전달될 JSON 페이로드
     */
    private record SchedulePayload(Long schedulerId, Long sellerId, String schedulerName) {}
}
