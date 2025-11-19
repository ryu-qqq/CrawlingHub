package com.ryuqq.crawlinghub.application.scheduler.fixture.usecase;

import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerHistoryPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.service.command.DeactivateSchedulerService;

public final class DeactivateSchedulerUseCaseFixture {

    private DeactivateSchedulerUseCaseFixture() {
    }

    public static DeactivateSchedulerService create(
            SchedulerPersistencePort schedulerPersistencePort,
            SchedulerQueryPort schedulerQueryPort,
            SchedulerHistoryPersistencePort schedulerHistoryPersistencePort,
            OutboxEventPersistencePort outboxEventPersistencePort,
            SchedulerAssembler schedulerAssembler
    ) {
        return new DeactivateSchedulerService(
                schedulerPersistencePort,
                schedulerQueryPort,
                schedulerHistoryPersistencePort,
                outboxEventPersistencePort,
                schedulerAssembler
        );
    }
}

