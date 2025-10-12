package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.util.CronExecutionCalculator;
import com.ryuqq.crawlinghub.application.schedule.util.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for updating an existing crawl schedule
 * Publishes ScheduleUpdatedEvent for EventBridge coordination
 */
@Service
public class UpdateScheduleUseCase {

    private final CrawlScheduleCommandPort scheduleCommandPort;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateScheduleUseCase(
            CrawlScheduleCommandPort scheduleCommandPort,
            ApplicationEventPublisher eventPublisher) {
        this.scheduleCommandPort = scheduleCommandPort;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Updates a schedule and publishes event if EventBridge sync needed
     * EventBridge coordination happens in event handler after transaction commit
     *
     * @param command the update command
     * @throws ScheduleNotFoundException if schedule not found
     * @throws InvalidCronExpressionException if new cron expression is invalid
     */
    @Transactional
    public void execute(UpdateScheduleCommand command) {
        // 1. Find existing schedule
        CrawlSchedule schedule = scheduleCommandPort.findById(ScheduleId.of(command.scheduleId()))
                .orElseThrow(() -> new ScheduleNotFoundException(command.scheduleId()));

        boolean cronChanged = false;

        // 2. Update cron expression if provided
        if (command.cronExpression() != null && !command.cronExpression().isBlank()) {
            CronExpressionValidator.validate(command.cronExpression());
            schedule.updateCronExpression(command.cronExpression());
            cronChanged = true;
        }

        // 3. Recalculate next execution time if cron changed
        if (cronChanged) {
            LocalDateTime nextExecution = CronExecutionCalculator.calculateNextExecution(
                    schedule.getCronExpression(),
                    schedule.getTimezone()
            );
            schedule.updateNextExecutionTime(nextExecution);
        }

        // 4. Save updated schedule (only DB operations in transaction)
        scheduleCommandPort.save(schedule);

        // 5. Update input parameters if provided
        if (command.inputParams() != null) {
            // Delete existing params
            scheduleCommandPort.deleteInputParamsByScheduleId(command.scheduleId());

            // Save new params
            if (!command.inputParams().isEmpty()) {
                List<ScheduleInputParam> inputParams = command.inputParams().stream()
                        .map(paramCmd -> {
                            try {
                                return ScheduleInputParam.create(
                                        command.scheduleId(),
                                        paramCmd.paramKey(),
                                        paramCmd.paramValue(),
                                        ParamType.valueOf(paramCmd.paramType())
                                );
                            } catch (IllegalArgumentException e) {
                                throw new InvalidParamTypeException(paramCmd.paramType(), e);
                            }
                        })
                        .collect(Collectors.toList());

                scheduleCommandPort.saveInputParams(inputParams);
            }
        }

        // 6. Publish event if EventBridge update needed (only for enabled schedules with cron changes)
        if (schedule.isEnabled() && cronChanged) {
            String awsCronExpression = CronExpressionValidator.convertToAwsCronExpression(
                    schedule.getCronExpression()
            );

            eventPublisher.publishEvent(new ScheduleUpdatedEvent(
                    command.scheduleId(),
                    schedule.getEventbridgeRuleName(),
                    awsCronExpression,
                    "Updated schedule: " + schedule.getScheduleName()
            ));
        }
    }
}
