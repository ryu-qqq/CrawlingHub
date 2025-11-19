package com.ryuqq.crawlinghub.application.scheduler.fixture.response;

import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerHistoryResponse;
import java.time.LocalDateTime;

public final class SchedulerHistoryResponseFixture {

    private SchedulerHistoryResponseFixture() {
    }

    public static SchedulerHistoryResponse create() {
        return SchedulerHistoryResponse.of(
            5001L,
            2001L,
            "cronExpression",
            "cron(0 12 * * ? *)",
            "cron(0 8 * * ? *)",
            LocalDateTime.of(2025, 11, 3, 0, 0)
        );
    }
}

