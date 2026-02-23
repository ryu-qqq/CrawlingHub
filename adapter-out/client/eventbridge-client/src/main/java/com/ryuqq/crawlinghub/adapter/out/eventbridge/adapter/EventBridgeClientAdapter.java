package com.ryuqq.crawlinghub.adapter.out.eventbridge.adapter;

import com.ryuqq.crawlinghub.adapter.out.eventbridge.exception.EventBridgePublishException;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.mapper.EventBridgeScheduleMapper;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.ScheduleState;
import software.amazon.awssdk.services.scheduler.model.Target;

@Component
@ConditionalOnProperty(prefix = "eventbridge", name = "target-arn")
public class EventBridgeClientAdapter implements EventBridgeClientPort {

    private static final Logger log = LoggerFactory.getLogger(EventBridgeClientAdapter.class);

    private final SchedulerClient schedulerClient;
    private final EventBridgeScheduleMapper mapper;

    public EventBridgeClientAdapter(
            SchedulerClient schedulerClient, EventBridgeScheduleMapper mapper) {
        this.schedulerClient = schedulerClient;
        this.mapper = mapper;
    }

    @Override
    public void syncFromOutBox(CrawlSchedulerOutBox outBox) {
        Long schedulerId = outBox.getSchedulerId();
        String scheduleName = mapper.toScheduleName(schedulerId);
        String cronExpression = mapper.toCronExpression(outBox.getCronExpression());
        Target target =
                mapper.toTarget(schedulerId, outBox.getSellerId(), outBox.getSchedulerName());

        try {
            if (outBox.getSchedulerStatus() == SchedulerStatus.INACTIVE) {
                updateSchedule(
                        scheduleName, cronExpression, target, ScheduleState.DISABLED, outBox);
            } else {
                createOrUpdateSchedule(scheduleName, cronExpression, target, outBox);
            }

            log.info(
                    "OutBox EventBridge 동기화 완료: outBoxId={}, schedulerId={}",
                    outBox.getOutBoxIdValue(),
                    schedulerId);
        } catch (EventBridgePublishException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "OutBox 동기화 실패: outBoxId={}, error={}",
                    outBox.getOutBoxIdValue(),
                    e.getMessage());
            throw new EventBridgePublishException("OutBox 동기화 실패", e);
        }
    }

    private void createOrUpdateSchedule(
            String scheduleName,
            String cronExpression,
            Target target,
            CrawlSchedulerOutBox outBox) {
        try {
            schedulerClient.createSchedule(
                    mapper.toCreateRequest(scheduleName, cronExpression, target, outBox));
        } catch (Exception e) {
            updateSchedule(scheduleName, cronExpression, target, ScheduleState.ENABLED, outBox);
        }
    }

    private void updateSchedule(
            String scheduleName,
            String cronExpression,
            Target target,
            ScheduleState state,
            CrawlSchedulerOutBox outBox) {
        schedulerClient.updateSchedule(
                mapper.toUpdateRequest(scheduleName, cronExpression, target, state, outBox));
    }
}
