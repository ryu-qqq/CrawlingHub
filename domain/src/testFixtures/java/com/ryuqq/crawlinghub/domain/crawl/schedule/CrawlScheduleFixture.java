package com.ryuqq.crawlinghub.domain.crawl.schedule;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * CrawlSchedule Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class CrawlScheduleFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final Long DEFAULT_SELLER_ID = 100L;
    private static final Clock DEFAULT_CLOCK = Clock.fixed(
        Instant.parse("2025-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    /**
     * 기본 CrawlSchedule 생성 (신규)
     *
     * @return CrawlSchedule
     */
    public static CrawlSchedule create() {
        return CrawlSchedule.forNew(
            MustitSellerId.of(DEFAULT_SELLER_ID),
            CronExpressionFixture.create()
        );
    }

    /**
     * ID를 가진 CrawlSchedule 생성
     *
     * @param id CrawlSchedule ID
     * @return CrawlSchedule
     */
    public static CrawlSchedule createWithId(Long id) {
        return CrawlSchedule.of(
            CrawlScheduleId.of(id),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            CronExpressionFixture.create(),
            ScheduleStatus.ACTIVE
        );
    }

    /**
     * 특정 셀러 ID로 CrawlSchedule 생성
     *
     * @param sellerId 셀러 ID
     * @return CrawlSchedule
     */
    public static CrawlSchedule createWithSellerId(Long sellerId) {
        return CrawlSchedule.forNew(
            MustitSellerId.of(sellerId),
            CronExpressionFixture.create()
        );
    }

    /**
     * 특정 Cron 표현식으로 CrawlSchedule 생성
     *
     * @param cronExpression Cron 표현식
     * @return CrawlSchedule
     */
    public static CrawlSchedule createWithCron(CronExpression cronExpression) {
        return CrawlSchedule.forNew(
            MustitSellerId.of(DEFAULT_SELLER_ID),
            cronExpression
        );
    }

    /**
     * ACTIVE 상태의 CrawlSchedule 생성
     *
     * @return CrawlSchedule
     */
    public static CrawlSchedule createActive() {
        return CrawlSchedule.of(
            CrawlScheduleId.of(DEFAULT_ID),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            CronExpressionFixture.create(),
            ScheduleStatus.ACTIVE
        );
    }

    /**
     * SUSPENDED 상태의 CrawlSchedule 생성
     *
     * @return CrawlSchedule
     */
    public static CrawlSchedule createSuspended() {
        return CrawlSchedule.of(
            CrawlScheduleId.of(DEFAULT_ID),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            CronExpressionFixture.create(),
            ScheduleStatus.SUSPENDED
        );
    }

    /**
     * 매시간 실행 스케줄 생성
     *
     * @return CrawlSchedule
     */
    public static CrawlSchedule createHourlySchedule() {
        return CrawlSchedule.forNew(
            MustitSellerId.of(DEFAULT_SELLER_ID),
            CronExpressionFixture.createHourly()
        );
    }

    /**
     * 매일 실행 스케줄 생성
     *
     * @return CrawlSchedule
     */
    public static CrawlSchedule createDailySchedule() {
        return CrawlSchedule.forNew(
            MustitSellerId.of(DEFAULT_SELLER_ID),
            CronExpressionFixture.createDaily()
        );
    }

    /**
     * DB reconstitute용 CrawlSchedule 생성
     *
     * @param id CrawlSchedule ID
     * @param sellerId 셀러 ID
     * @param status 스케줄 상태
     * @return CrawlSchedule
     */
    public static CrawlSchedule reconstitute(
        Long id,
        Long sellerId,
        ScheduleStatus status
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return CrawlSchedule.reconstitute(
            CrawlScheduleId.of(id),
            MustitSellerId.of(sellerId),
            CronExpressionFixture.create(),
            status,
            now.plusHours(1),
            null,
            now,
            now
        );
    }

    /**
     * 다음 실행 시간이 설정된 CrawlSchedule 생성
     *
     * @param nextExecutionTime 다음 실행 시간
     * @return CrawlSchedule
     */
    public static CrawlSchedule createWithNextExecution(LocalDateTime nextExecutionTime) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return CrawlSchedule.reconstitute(
            CrawlScheduleId.of(DEFAULT_ID),
            MustitSellerId.of(DEFAULT_SELLER_ID),
            CronExpressionFixture.create(),
            ScheduleStatus.ACTIVE,
            nextExecutionTime,
            null,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 CrawlSchedule 생성
     *
     * @param id CrawlSchedule ID (null 가능)
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     * @param status 스케줄 상태
     * @return CrawlSchedule
     */
    public static CrawlSchedule createCustom(
        Long id,
        Long sellerId,
        CronExpression cronExpression,
        ScheduleStatus status
    ) {
        if (id == null) {
            return CrawlSchedule.forNew(
                MustitSellerId.of(sellerId),
                cronExpression
            );
        }
        return CrawlSchedule.of(
            CrawlScheduleId.of(id),
            MustitSellerId.of(sellerId),
            cronExpression,
            status
        );
    }
}
