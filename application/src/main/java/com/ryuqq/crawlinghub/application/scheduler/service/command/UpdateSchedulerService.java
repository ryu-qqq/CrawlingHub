package com.ryuqq.crawlinghub.application.scheduler.service.command;

import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.UpdateSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.application.scheduler.port.in.command.UpdateSchedulerUseCase;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerHistoryPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class UpdateSchedulerService implements UpdateSchedulerUseCase {

    private final SchedulerPersistencePort schedulerPersistencePort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SchedulerHistoryPersistencePort schedulerHistoryPersistencePort;
    private final OutboxEventPersistencePort outboxEventPersistencePort;
    private final SchedulerAssembler schedulerAssembler;

    public UpdateSchedulerService(
        SchedulerPersistencePort schedulerPersistencePort,
        SchedulerQueryPort schedulerQueryPort,
        SchedulerHistoryPersistencePort schedulerHistoryPersistencePort,
        OutboxEventPersistencePort outboxEventPersistencePort,
        SchedulerAssembler schedulerAssembler
    ) {
        this.schedulerPersistencePort = schedulerPersistencePort;
        this.schedulerQueryPort = schedulerQueryPort;
        this.schedulerHistoryPersistencePort = schedulerHistoryPersistencePort;
        this.outboxEventPersistencePort = outboxEventPersistencePort;
        this.schedulerAssembler = schedulerAssembler;
    }

    @Override
    public SchedulerResponse execute(UpdateSchedulerCommand command) {
        CrawlingScheduler scheduler = schedulerQueryPort.findById(SchedulerId.of(command.schedulerId()))
            .orElseThrow(() -> new IllegalArgumentException("Scheduler not found"));

        String updatedName = command.schedulerName() != null ? command.schedulerName() : scheduler.getSchedulerName();
        CronExpression updatedCron = command.cronExpression() != null
            ? CronExpression.of(command.cronExpression())
            : scheduler.getCronExpression();
        SchedulerStatus updatedStatus = command.status() != null ? command.status() : scheduler.getStatus();

        scheduler.update(updatedName, updatedCron, updatedStatus);

        CrawlingScheduler saved = schedulerPersistencePort.saveScheduler(scheduler);

        SchedulerHistory history = SchedulerHistory.of(
            null,
            saved.getSchedulerId(),
            "SCHEDULER",
            "previous",
            "current",
            LocalDateTime.now()
        );
        schedulerHistoryPersistencePort.saveSchedulerHistory(history);

        OutboxEvent outboxEvent = OutboxEvent.of(
            null,
            OutboxEventType.SCHEDULER_UPDATED,
            saved.getSchedulerId(),
            "payload",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        );
        outboxEventPersistencePort.saveOutboxEvent(outboxEvent);

        return schedulerAssembler.toResponse(saved);
    }
}

