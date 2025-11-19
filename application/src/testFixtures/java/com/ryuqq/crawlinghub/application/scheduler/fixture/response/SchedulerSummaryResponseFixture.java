package com.ryuqq.crawlinghub.application.scheduler.fixture.response;

import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerSummaryResponse;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;

public final class SchedulerSummaryResponseFixture {

    private SchedulerSummaryResponseFixture() {
    }

    public static SchedulerSummaryResponse create() {
        return SchedulerSummaryResponse.from(CrawlingSchedulerFixture.aReconstitutedScheduler());
    }
}

