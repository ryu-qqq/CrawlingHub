package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDisabledEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for disabling a crawl schedule
 * Publishes ScheduleDisabledEvent for EventBridge coordination
 */
@Service
public class DisableScheduleUseCase {

    private final CrawlScheduleCommandPort scheduleCommandPort;
    private final ApplicationEventPublisher eventPublisher;

    public DisableScheduleUseCase(
            CrawlScheduleCommandPort scheduleCommandPort,
            ApplicationEventPublisher eventPublisher) {
        this.scheduleCommandPort = scheduleCommandPort;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Disables a schedule by updating database and publishing event
     * EventBridge coordination happens in event handler after transaction commit
     *
     * @param scheduleId the schedule ID to disable
     * @throws ScheduleNotFoundException if schedule not found
     * @throws InvalidScheduleException if schedule cannot be disabled
     */
    @Transactional
    public void execute(Long scheduleId) {
        // 1. Find existing schedule
        CrawlSchedule schedule = scheduleCommandPort.findById(ScheduleId.of(scheduleId))
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        // 2. Check if already disabled
        if (!schedule.isEnabled()) {
            throw new InvalidScheduleException("Schedule is already disabled: " + scheduleId);
        }

        // 3. Update schedule status in database
        String ruleName = schedule.getEventbridgeRuleName();
        schedule.disable();
        scheduleCommandPort.save(schedule);

        // 4. Publish event (EventBridge operations will happen after transaction commits)
        eventPublisher.publishEvent(new ScheduleDisabledEvent(scheduleId, ruleName));
    }
}
