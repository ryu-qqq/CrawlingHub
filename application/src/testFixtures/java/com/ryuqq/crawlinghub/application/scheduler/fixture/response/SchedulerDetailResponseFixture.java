package com.ryuqq.crawlinghub.application.scheduler.fixture.response;

import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerDetailResponse;
import static com.ryuqq.crawlinghub.application.scheduler.fixture.response.SchedulerResponseFixture.detailResponse;

public final class SchedulerDetailResponseFixture {

    private SchedulerDetailResponseFixture() {
    }

    public static SchedulerDetailResponse create() {
        return detailResponse();
    }
}

