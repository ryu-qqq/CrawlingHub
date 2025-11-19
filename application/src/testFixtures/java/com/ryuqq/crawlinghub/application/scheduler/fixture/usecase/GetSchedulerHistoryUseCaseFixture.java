package com.ryuqq.crawlinghub.application.scheduler.fixture.usecase;

import com.ryuqq.crawlinghub.application.port.out.query.SchedulerHistoryQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.service.query.GetSchedulerHistoryService;

public final class GetSchedulerHistoryUseCaseFixture {

    private GetSchedulerHistoryUseCaseFixture() {
    }

    public static GetSchedulerHistoryService create(
        SchedulerHistoryQueryPort schedulerHistoryQueryPort,
        SchedulerAssembler schedulerAssembler
    ) {
        return new GetSchedulerHistoryService(
            schedulerHistoryQueryPort,
            schedulerAssembler
        );
    }
}

