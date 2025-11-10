package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDeletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for deleting a crawl schedule
 * Publishes ScheduleDeletedEvent for EventBridge cleanup
 */
@Service
public class DeleteScheduleUseCase {

    private final CrawlScheduleCommandPort scheduleCommandPort;
    private final ApplicationEventPublisher eventPublisher;

    public DeleteScheduleUseCase(
            CrawlScheduleCommandPort scheduleCommandPort,
            ApplicationEventPublisher eventPublisher) {
        this.scheduleCommandPort = scheduleCommandPort;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Deletes a schedule from database and publishes event
     * EventBridge cleanup happens in event handler after transaction commit
     *
     * @param scheduleId the schedule ID to delete
     * @throws ScheduleNotFoundException if schedule not found
     */
    @Transactional
    public void execute(Long scheduleId) {
        // 1. Find existing schedule
        CrawlSchedule schedule = scheduleCommandPort.findById(ScheduleId.of(scheduleId))
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        // 2. Capture state before deletion
        boolean wasEnabled = schedule.isEnabled();
        String ruleName = schedule.getEventbridgeRuleName();

        // 3. Delete from database (only DB operations in transaction)
        scheduleCommandPort.deleteInputParamsByScheduleId(scheduleId);
        scheduleCommandPort.deleteById(ScheduleId.of(scheduleId));

        // 4. Publish event if EventBridge cleanup is needed
        if (wasEnabled) {
            eventPublisher.publishEvent(new ScheduleDeletedEvent(scheduleId, ruleName, true));
        }
    }
}
