package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;

/**
 * CrawlSchedulerOutBox 테스트 Fixture
 *
 * <p>Object Mother 패턴: 테스트용 CrawlSchedulerOutBox 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlSchedulerOutBoxFixture {

    private static final Instant DEFAULT_INSTANT = FixedClock.aDefaultClock().instant();

    private CrawlSchedulerOutBoxFixture() {
        throw new UnsupportedOperationException("Fixture 클래스입니다.");
    }

    /**
     * PENDING 상태의 OutBox 생성
     *
     * @return PENDING 상태 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox aPendingOutBox() {
        Instant now = DEFAULT_INSTANT;
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(1L),
                CrawlSchedulerHistoryId.of(1L),
                CrawlSchedulerOubBoxStatus.PENDING,
                1L,
                1L,
                "test-scheduler",
                "cron(0 0 * * ? *)",
                SchedulerStatus.ACTIVE,
                null,
                0L,
                now,
                null);
    }

    /**
     * COMPLETED 상태의 OutBox 생성
     *
     * @return COMPLETED 상태 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox aCompletedOutBox() {
        Instant now = DEFAULT_INSTANT;
        Instant fiveMinutesAgo = now.minusSeconds(300);
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(2L),
                CrawlSchedulerHistoryId.of(2L),
                CrawlSchedulerOubBoxStatus.COMPLETED,
                2L,
                2L,
                "test-scheduler-2",
                "cron(0 0 * * ? *)",
                SchedulerStatus.ACTIVE,
                null,
                1L,
                fiveMinutesAgo,
                now);
    }

    /**
     * FAILED 상태의 OutBox 생성
     *
     * @return FAILED 상태 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox aFailedOutBox() {
        Instant now = DEFAULT_INSTANT;
        Instant tenMinutesAgo = now.minusSeconds(600);
        Instant fiveMinutesAgo = now.minusSeconds(300);
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(3L),
                CrawlSchedulerHistoryId.of(3L),
                CrawlSchedulerOubBoxStatus.FAILED,
                3L,
                3L,
                "test-scheduler-3",
                "cron(0 0 * * ? *)",
                SchedulerStatus.ACTIVE,
                "EventBridge connection failed",
                1L,
                tenMinutesAgo,
                fiveMinutesAgo);
    }
}
