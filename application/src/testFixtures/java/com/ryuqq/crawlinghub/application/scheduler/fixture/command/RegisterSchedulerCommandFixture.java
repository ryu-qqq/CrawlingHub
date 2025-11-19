package com.ryuqq.crawlinghub.application.scheduler.fixture.command;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.RegisterSchedulerCommand;

public final class RegisterSchedulerCommandFixture {

    private RegisterSchedulerCommandFixture() {
    }

    public static RegisterSchedulerCommand create() {
        return new RegisterSchedulerCommand(1L, "daily-crawling", "cron(0 0 * * ? *)");
    }
}

