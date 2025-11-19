package com.ryuqq.crawlinghub.application.scheduler.service.command;

import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.DeactivateSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.application.scheduler.port.in.command.DeactivateSchedulerUseCase;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerHistoryPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class DeactivateSchedulerService implements DeactivateSchedulerUseCase {

    private final SchedulerPersistencePort schedulerPersistencePort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SchedulerHistoryPersistencePort schedulerHistoryPersistencePort;
    private final OutboxEventPersistencePort outboxEventPersistencePort;
    private final SchedulerAssembler schedulerAssembler;

    public DeactivateSchedulerService(
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
    public SchedulerResponse execute(DeactivateSchedulerCommand command) {
        CrawlingScheduler scheduler = schedulerQueryPort.findById(SchedulerId.of(command.schedulerId()))
            .orElseThrow(() -> new IllegalArgumentException("Scheduler not found"));

        scheduler.update(
            scheduler.getSchedulerName(),
            scheduler.getCronExpression(),
            SchedulerStatus.INACTIVE
        );

        CrawlingScheduler saved = schedulerPersistencePort.saveScheduler(scheduler);

        schedulerHistoryPersistencePort.saveSchedulerHistory(
            SchedulerHistory.of(
                null,
                saved.getSchedulerId(),
                "status",
                SchedulerStatus.ACTIVE.name(),
                SchedulerStatus.INACTIVE.name(),
                LocalDateTime.now()
            )
        );

        outboxEventPersistencePort.saveOutboxEvent(OutboxEvent.of(
            null,
            OutboxEventType.SCHEDULER_DELETED,
            saved.getSchedulerId(),
            "payload",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        ));

        return schedulerAssembler.toResponse(saved);
    }
}

