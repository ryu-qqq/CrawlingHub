package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.crawlinghub.domain.common.Clock;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * CrawlSchedulerOutBox 테스트 Fixture
 *
 * <p>Object Mother 패턴: 테스트용 CrawlSchedulerOutBox 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlSchedulerOutBoxFixture {

    private CrawlSchedulerOutBoxFixture() {
        throw new UnsupportedOperationException("Fixture 클래스입니다.");
    }

    /**
     * PENDING 상태의 OutBox 생성
     *
     * @return PENDING 상태 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox aPendingOutBox() {
        return CrawlSchedulerOutBox.of(
                CrawlSchedulerOutBoxId.of(1L),
                CrawlSchedulerHistoryId.of(1L),
                CrawlSchedulerOubBoxStatus.PENDING,
                "{\"schedulerId\": 1, \"action\": \"CREATE\"}",
                null,
                0L,
                LocalDateTime.now(),
                null,
                new FixedClock());
    }

    /**
     * COMPLETED 상태의 OutBox 생성
     *
     * @return COMPLETED 상태 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox aCompletedOutBox() {
        return CrawlSchedulerOutBox.of(
                CrawlSchedulerOutBoxId.of(2L),
                CrawlSchedulerHistoryId.of(2L),
                CrawlSchedulerOubBoxStatus.COMPLETED,
                "{\"schedulerId\": 2, \"action\": \"CREATE\"}",
                null,
                1L,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now(),
                new FixedClock());
    }

    /**
     * FAILED 상태의 OutBox 생성
     *
     * @return FAILED 상태 CrawlSchedulerOutBox
     */
    public static CrawlSchedulerOutBox aFailedOutBox() {
        return CrawlSchedulerOutBox.of(
                CrawlSchedulerOutBoxId.of(3L),
                CrawlSchedulerHistoryId.of(3L),
                CrawlSchedulerOubBoxStatus.FAILED,
                "{\"schedulerId\": 3, \"action\": \"CREATE\"}",
                "EventBridge connection failed",
                1L,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(5),
                new FixedClock());
    }

    /**
     * 테스트용 고정 시간 Clock 구현
     */
    private static class FixedClock implements Clock {
        @Override
        public Instant now() {
            return Instant.now();
        }
    }
}
