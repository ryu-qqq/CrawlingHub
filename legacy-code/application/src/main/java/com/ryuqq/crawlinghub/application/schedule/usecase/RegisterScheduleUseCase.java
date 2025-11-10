package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.util.CronExecutionCalculator;
import com.ryuqq.crawlinghub.application.schedule.util.CronExpressionValidator;
import com.ryuqq.crawlinghub.application.workflow.usecase.WorkflowNotFoundException;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for registering a new crawl schedule
 * Validates workflow existence, cron expression, and creates schedule with input params
 */
@Service
public class RegisterScheduleUseCase {

    private final CrawlScheduleCommandPort scheduleCommandPort;

    public RegisterScheduleUseCase(CrawlScheduleCommandPort scheduleCommandPort) {
        this.scheduleCommandPort = scheduleCommandPort;
    }

    /**
     * Registers a new schedule
     *
     * @param command the registration command
     * @return the created schedule ID
     * @throws WorkflowNotFoundException if workflow doesn't exist
     * @throws InvalidCronExpressionException if cron expression is invalid
     */
    @Transactional
    public ScheduleId execute(RegisterScheduleCommand command) {
        // 1. Validate cron expression
        CronExpressionValidator.validate(command.cronExpression());

        // 2. Create schedule domain object
        WorkflowId workflowId = WorkflowId.of(command.workflowId());
        CrawlSchedule schedule = CrawlSchedule.create(
                workflowId,
                command.scheduleName(),
                command.cronExpression(),
                command.timezone()
        );

        // 3. Calculate next execution time
        LocalDateTime nextExecution = CronExecutionCalculator.calculateNextExecution(
                command.cronExpression(),
                command.timezone()
        );
        schedule.updateNextExecutionTime(nextExecution);

        // 4. Initially disable the schedule (must be explicitly enabled)
        schedule.disable();

        // 5. Save schedule
        CrawlSchedule savedSchedule = scheduleCommandPort.save(schedule);

        // 6. Save input parameters if provided
        if (command.inputParams() != null && !command.inputParams().isEmpty()) {
            List<ScheduleInputParam> inputParams = command.inputParams().stream()
                    .map(paramCmd -> {
                        try {
                            return ScheduleInputParam.create(
                                    savedSchedule.getScheduleId().value(),
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

        return savedSchedule.getScheduleId();
    }
}
