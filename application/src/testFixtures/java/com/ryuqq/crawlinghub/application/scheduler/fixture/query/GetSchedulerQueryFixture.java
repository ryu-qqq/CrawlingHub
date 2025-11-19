package com.ryuqq.crawlinghub.application.scheduler.fixture.query;

import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerQuery;

public final class GetSchedulerQueryFixture {

    private GetSchedulerQueryFixture() {
    }

    public static GetSchedulerQuery create() {
        return new GetSchedulerQuery(1L);
    }
}

