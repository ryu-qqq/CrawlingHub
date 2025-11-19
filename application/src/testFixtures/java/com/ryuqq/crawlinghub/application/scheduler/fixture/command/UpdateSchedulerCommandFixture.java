package com.ryuqq.crawlinghub.application.scheduler.fixture.command;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.UpdateSchedulerCommand;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

public final class UpdateSchedulerCommandFixture {

    private UpdateSchedulerCommandFixture() {
    }

    public static UpdateSchedulerCommand create() {
        return new UpdateSchedulerCommand(
            10L,
            "weekly-crawling",
            "cron(0 0 * * ? *)",
            SchedulerStatus.ACTIVE
        );
    }
}

