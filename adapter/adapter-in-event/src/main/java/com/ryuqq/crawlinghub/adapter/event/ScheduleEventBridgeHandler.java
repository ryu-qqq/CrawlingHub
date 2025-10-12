package com.ryuqq.crawlinghub.adapter.event;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.port.EventBridgePort;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDeletedEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDisabledEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleEnabledEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for schedule lifecycle events.
 * Coordinates EventBridge operations after database transaction commits
 * Implements compensation transaction for EventBridge failures
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@Component
public class ScheduleEventBridgeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleEventBridgeHandler.class);

    private final EventBridgePort eventBridgePort;
    private final CrawlScheduleCommandPort scheduleCommandPort;

    public ScheduleEventBridgeHandler(
            EventBridgePort eventBridgePort,
            CrawlScheduleCommandPort scheduleCommandPort) {
        this.eventBridgePort = eventBridgePort;
        this.scheduleCommandPort = scheduleCommandPort;
    }

    /**
     * Handles schedule enabled event
     * Creates and enables EventBridge rule after DB transaction commits
     * If EventBridge operations fail, reverts schedule status (compensation transaction)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleEnabled(ScheduleEnabledEvent event) {
        LOG.info("Handling schedule enabled event: scheduleId={}, ruleName={}",
                event.scheduleId(), event.ruleName());

        try {
            // Create EventBridge rule (initially disabled)
            eventBridgePort.createRule(
                    event.ruleName(),
                    event.cronExpression(),
                    "Crawling schedule: " + event.scheduleName()
            );

            // Add target to rule
            eventBridgePort.addTarget(event.ruleName(), event.targetInput());

            // Enable the rule
            eventBridgePort.enableRule(event.ruleName());

            LOG.info("Successfully enabled schedule in EventBridge: scheduleId={}, ruleName={}",
                    event.scheduleId(), event.ruleName());

        } catch (Exception e) {
            LOG.error("Failed to enable schedule in EventBridge: scheduleId={}, ruleName={}",
                    event.scheduleId(), event.ruleName(), e);

            // Compensation transaction: Revert schedule status in database
            compensateScheduleEnable(event.scheduleId(), e);
        }
    }

    /**
     * Handles schedule disabled event
     * Disables EventBridge rule after DB transaction commits
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleDisabled(ScheduleDisabledEvent event) {
        LOG.info("Handling schedule disabled event: scheduleId={}, ruleName={}",
                event.scheduleId(), event.ruleName());

        try {
            if (eventBridgePort.ruleExists(event.ruleName())) {
                eventBridgePort.disableRule(event.ruleName());
                LOG.info("Successfully disabled schedule in EventBridge: scheduleId={}, ruleName={}",
                        event.scheduleId(), event.ruleName());
            } else {
                LOG.warn("EventBridge rule not found for disable operation: ruleName={}", event.ruleName());
            }

        } catch (Exception e) {
            LOG.error("Failed to disable schedule in EventBridge: scheduleId={}, ruleName={}",
                    event.scheduleId(), event.ruleName(), e);

            // Compensation transaction: Revert schedule status
            compensateScheduleDisable(event.scheduleId(), e);
        }
    }

    /**
     * Handles schedule deleted event
     * Removes EventBridge rule and targets after DB transaction commits
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleDeleted(ScheduleDeletedEvent event) {
        LOG.info("Handling schedule deleted event: scheduleId={}, ruleName={}, wasEnabled={}",
                event.scheduleId(), event.ruleName(), event.wasEnabled());

        // Only cleanup EventBridge if schedule was enabled
        if (!event.wasEnabled()) {
            LOG.debug("Schedule was not enabled, skipping EventBridge cleanup: scheduleId={}",
                    event.scheduleId());
            return;
        }

        try {
            if (eventBridgePort.ruleExists(event.ruleName())) {
                // Remove targets first
                eventBridgePort.removeTargets(event.ruleName());

                // Then delete rule
                eventBridgePort.deleteRule(event.ruleName());

                LOG.info("Successfully deleted schedule from EventBridge: scheduleId={}, ruleName={}",
                        event.scheduleId(), event.ruleName());
            } else {
                LOG.warn("EventBridge rule not found for delete operation: ruleName={}", event.ruleName());
            }

        } catch (Exception e) {
            LOG.error("Failed to delete schedule from EventBridge: scheduleId={}, ruleName={}",
                    event.scheduleId(), event.ruleName(), e);

            // Note: Cannot compensate for delete operation as DB record is already gone
            // Manual intervention may be required to clean up orphaned EventBridge rules
            LOG.error("COMPENSATION NOT POSSIBLE: Schedule already deleted from database. "
                     + "Manual cleanup of EventBridge rule may be required: ruleName={}", event.ruleName());
        }
    }

    /**
     * Handles schedule updated event
     * Updates EventBridge rule after DB transaction commits
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleUpdated(ScheduleUpdatedEvent event) {
        LOG.info("Handling schedule updated event: scheduleId={}, ruleName={}",
                event.scheduleId(), event.ruleName());

        try {
            if (eventBridgePort.ruleExists(event.ruleName())) {
                eventBridgePort.updateRule(
                        event.ruleName(),
                        event.cronExpression(),
                        event.description()
                );

                LOG.info("Successfully updated schedule in EventBridge: scheduleId={}, ruleName={}",
                        event.scheduleId(), event.ruleName());
            } else {
                LOG.warn("EventBridge rule not found for update operation: ruleName={}", event.ruleName());
            }

        } catch (Exception e) {
            LOG.error("Failed to update schedule in EventBridge: scheduleId={}, ruleName={}",
                    event.scheduleId(), event.ruleName(), e);

            // Note: Update failures are non-critical
            // The DB state is already correct, EventBridge is just out of sync
            LOG.warn("EventBridge update failed but DB state is correct. "
                    + "Consider manual sync or retry mechanism for scheduleId={}", event.scheduleId());
        }
    }

    /**
     * Compensation transaction for enable operation failure
     * Reverts schedule to disabled state in database
     */
    private void compensateScheduleEnable(Long scheduleId, Exception originalException) {
        try {
            LOG.warn("Starting compensation transaction for schedule enable failure: scheduleId={}", scheduleId);

            scheduleCommandPort.findById(ScheduleId.of(scheduleId)).ifPresentOrElse(
                    schedule -> {
                        schedule.disable();
                        scheduleCommandPort.save(schedule);
                        LOG.info("Compensation completed: Schedule reverted to disabled state: scheduleId={}",
                                scheduleId);
                    },
                    () -> LOG.error("Compensation failed: Schedule not found in database: scheduleId={}",
                            scheduleId)
            );

        } catch (Exception compensationException) {
            LOG.error("CRITICAL: Compensation transaction failed for schedule enable: scheduleId={}. "
                     + "Database shows schedule as enabled but EventBridge operation failed. "
                     + "Manual intervention required.",
                     scheduleId, compensationException);

            // Suppress exception to avoid breaking the event listener chain
            // Manual intervention will be needed to fix the inconsistent state
        }
    }

    /**
     * Compensation transaction for disable operation failure
     * Reverts schedule to enabled state in database
     */
    private void compensateScheduleDisable(Long scheduleId, Exception originalException) {
        try {
            LOG.warn("Starting compensation transaction for schedule disable failure: scheduleId={}", scheduleId);

            scheduleCommandPort.findById(ScheduleId.of(scheduleId)).ifPresentOrElse(
                    schedule -> {
                        schedule.enable();
                        scheduleCommandPort.save(schedule);
                        LOG.info("Compensation completed: Schedule reverted to enabled state: scheduleId={}",
                                scheduleId);
                    },
                    () -> LOG.error("Compensation failed: Schedule not found in database: scheduleId={}",
                            scheduleId)
            );

        } catch (Exception compensationException) {
            LOG.error("CRITICAL: Compensation transaction failed for schedule disable: scheduleId={}. "
                     + "Database shows schedule as disabled but EventBridge operation failed. "
                     + "Manual intervention required.",
                     scheduleId, compensationException);

            // Suppress exception to avoid breaking the event listener chain
        }
    }
}
