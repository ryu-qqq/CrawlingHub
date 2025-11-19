package com.ryuqq.crawlinghub.application.scheduler.fixture.usecase;

import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.service.query.ListSchedulersService;

public final class ListSchedulersUseCaseFixture {

    private ListSchedulersUseCaseFixture() {
    }

    public static ListSchedulersService create(
        SchedulerQueryPort schedulerQueryPort,
        SchedulerAssembler schedulerAssembler
    ) {
        return new ListSchedulersService(
            schedulerQueryPort,
            schedulerAssembler
        );
    }
}

