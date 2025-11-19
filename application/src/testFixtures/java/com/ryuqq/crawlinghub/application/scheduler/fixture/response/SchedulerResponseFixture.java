package com.ryuqq.crawlinghub.application.scheduler.fixture.response;

import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;

public final class SchedulerResponseFixture {

    private SchedulerResponseFixture() {
    }

    public static SchedulerResponse create() {
        return SchedulerResponse.from(
            CrawlingSchedulerFixture.aReconstitutedScheduler(),
            "arn:aws:events:ap-northeast-2:123456789012:rule/fixture"
        );
    }

    public static SchedulerResponse withoutEventBridgeRule() {
        return SchedulerResponse.from(
            CrawlingSchedulerFixture.aReconstitutedScheduler(),
            null
        );
    }

    public static SchedulerDetailResponse detailResponse() {
        return SchedulerDetailResponse.from(
            CrawlingSchedulerFixture.aReconstitutedScheduler(),
            "arn:aws:events:rule/detail"
        );
    }

    public static SchedulerResponse fromScheduler(
        CrawlingScheduler scheduler,
        String eventBridgeRuleName
    ) {
        return SchedulerResponse.from(scheduler, eventBridgeRuleName);
    }
}

