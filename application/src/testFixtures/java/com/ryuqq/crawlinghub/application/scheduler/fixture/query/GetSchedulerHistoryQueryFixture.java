package com.ryuqq.crawlinghub.application.scheduler.fixture.query;

import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerHistoryQuery;

public final class GetSchedulerHistoryQueryFixture {

    private GetSchedulerHistoryQueryFixture() {
    }

    public static GetSchedulerHistoryQuery create() {
        return new GetSchedulerHistoryQuery(77L, 1, 50);
    }
}

