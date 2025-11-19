package com.ryuqq.crawlinghub.application.scheduler.fixture.command;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.DeactivateSchedulerCommand;

public final class DeactivateSchedulerCommandFixture {

    private DeactivateSchedulerCommandFixture() {
    }

    public static DeactivateSchedulerCommand create() {
        return new DeactivateSchedulerCommand(15L);
    }
}

