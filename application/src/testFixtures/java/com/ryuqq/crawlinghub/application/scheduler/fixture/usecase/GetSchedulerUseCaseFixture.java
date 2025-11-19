package com.ryuqq.crawlinghub.application.scheduler.fixture.usecase;

import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.service.query.GetSchedulerService;

public final class GetSchedulerUseCaseFixture {

    private GetSchedulerUseCaseFixture() {
    }

    public static GetSchedulerService create(
        SchedulerQueryPort schedulerQueryPort,
        SchedulerAssembler schedulerAssembler
    ) {
        return new GetSchedulerService(
            schedulerQueryPort,
            schedulerAssembler
        );
    }
}

