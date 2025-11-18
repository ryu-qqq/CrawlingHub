package com.ryuqq.crawlinghub.domain.fixture.eventbridge;

import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

/**
 * SchedulerStatus 테스트 픽스처
 *
 * <p>Enum 값을 중앙에서 관리하여 테스트 중복을 제거합니다.</p>
 */
public final class SchedulerStatusFixture {

    private SchedulerStatusFixture() {
    }

    public static SchedulerStatus forNew() {
        return SchedulerStatus.PENDING;
    }

    public static SchedulerStatus of() {
        return SchedulerStatus.ACTIVE;
    }

    public static SchedulerStatus reconstitute() {
        return SchedulerStatus.INACTIVE;
    }

    public static SchedulerStatus pending() {
        return SchedulerStatus.PENDING;
    }

    public static SchedulerStatus active() {
        return SchedulerStatus.ACTIVE;
    }

    public static SchedulerStatus inactive() {
        return SchedulerStatus.INACTIVE;
    }
}

