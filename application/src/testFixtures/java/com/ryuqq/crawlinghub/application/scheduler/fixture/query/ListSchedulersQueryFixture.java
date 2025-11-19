package com.ryuqq.crawlinghub.application.scheduler.fixture.query;

import com.ryuqq.crawlinghub.application.scheduler.dto.query.ListSchedulersQuery;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

public final class ListSchedulersQueryFixture {

    private ListSchedulersQueryFixture() {
    }

    public static ListSchedulersQuery create() {
        return new ListSchedulersQuery(99L, SchedulerStatus.ACTIVE, 0, 20);
    }
}

