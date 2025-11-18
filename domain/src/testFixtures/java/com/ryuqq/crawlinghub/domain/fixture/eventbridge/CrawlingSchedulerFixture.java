package com.ryuqq.crawlinghub.domain.fixture.eventbridge;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

public final class CrawlingSchedulerFixture {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-11-18T00:00:00Z"), ZoneOffset.UTC);
    private static final Long DEFAULT_SCHEDULER_ID = 1001L;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2025, 11, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2025, 11, 2, 0, 0);

    private CrawlingSchedulerFixture() {
    }

    public static CrawlingScheduler forNew() {
        return CrawlingScheduler.forNew(
            aSellerId(),
            aSchedulerName(),
            aCronExpression(),
            aFixedClock()
        );
    }

    public static CrawlingScheduler of() {
        return forNew();
    }

    public static CrawlingScheduler reconstitute() {
        return forNew();
    }

    public static CrawlingScheduler aCrawlingScheduler() {
        return forNew();
    }

    public static CrawlingScheduler aReconstitutedScheduler() {
        return CrawlingScheduler.reconstitute(
            aSchedulerId(),
            aSellerId(),
            aSchedulerName(),
            aCronExpression(),
            SchedulerStatusFixture.inactive(),
            aCreatedAt(),
            anUpdatedAt(),
            aFixedClock()
        );
    }

    public static Long aSchedulerId() {
        return DEFAULT_SCHEDULER_ID;
    }

    public static long aSellerId() {
        return 1L;
    }

    public static String aSchedulerName() {
        return "fixture-scheduler";
    }

    public static CronExpression aCronExpression() {
        return CronExpressionFixture.aCronExpression();
    }

    public static CronExpression anotherCronExpression() {
        return CronExpression.of("cron(0 8 * * ? *)");
    }

    public static SchedulerStatus aSchedulerStatus() {
        return SchedulerStatusFixture.pending();
    }

    public static Clock aFixedClock() {
        return FIXED_CLOCK;
    }

    public static LocalDateTime aCreatedAt() {
        return DEFAULT_CREATED_AT;
    }

    public static LocalDateTime anUpdatedAt() {
        return DEFAULT_UPDATED_AT;
    }
}

