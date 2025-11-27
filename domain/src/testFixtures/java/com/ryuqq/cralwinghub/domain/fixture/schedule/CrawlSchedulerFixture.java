package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.crawlinghub.domain.common.Clock;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * CrawlScheduler Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlSchedulerFixture {

    private static final Clock DEFAULT_CLOCK = FixedClock.aDefaultClock();

    /**
     * 신규 활성 스케줄러 생성 (ID = null)
     *
     * @return 신규 CrawlScheduler (ID = null, ACTIVE)
     */
    public static CrawlScheduler aNewActiveScheduler() {
        return CrawlScheduler.forNew(
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                DEFAULT_CLOCK);
    }

    /**
     * 신규 활성 스케줄러 생성 (특정 Clock)
     *
     * @param clock 시간 제어
     * @return 신규 CrawlScheduler (ID = null, ACTIVE)
     */
    public static CrawlScheduler aNewActiveScheduler(Clock clock) {
        return CrawlScheduler.forNew(
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                clock);
    }

    /**
     * 신규 활성 스케줄러 생성 (특정 셀러)
     *
     * @param sellerId 셀러 ID
     * @return 신규 CrawlScheduler (ID = null, ACTIVE)
     */
    public static CrawlScheduler aNewActiveScheduler(SellerId sellerId) {
        return CrawlScheduler.forNew(
                sellerId,
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                DEFAULT_CLOCK);
    }

    /**
     * 할당된 ID를 가진 활성 스케줄러 생성
     *
     * @return CrawlScheduler (ID = 1L, ACTIVE)
     */
    public static CrawlScheduler anActiveScheduler() {
        LocalDateTime now = LocalDateTime.ofInstant(DEFAULT_CLOCK.now(), ZoneId.systemDefault());
        return CrawlScheduler.of(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.ACTIVE,
                now,
                now,
                DEFAULT_CLOCK);
    }

    /**
     * 할당된 ID를 가진 활성 스케줄러 생성 (특정 ID)
     *
     * @param id 스케줄러 ID
     * @return CrawlScheduler (ACTIVE)
     */
    public static CrawlScheduler anActiveScheduler(Long id) {
        LocalDateTime now = LocalDateTime.ofInstant(DEFAULT_CLOCK.now(), ZoneId.systemDefault());
        return CrawlScheduler.of(
                CrawlSchedulerId.of(id),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.ACTIVE,
                now,
                now,
                DEFAULT_CLOCK);
    }

    /**
     * 할당된 ID를 가진 활성 스케줄러 생성 (특정 Clock)
     *
     * @param clock 시간 제어
     * @return CrawlScheduler (ID = 1L, ACTIVE)
     */
    public static CrawlScheduler anActiveScheduler(Clock clock) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        return CrawlScheduler.of(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.ACTIVE,
                now,
                now,
                clock);
    }

    /**
     * 할당된 ID를 가진 비활성 스케줄러 생성
     *
     * @return CrawlScheduler (ID = 1L, INACTIVE)
     */
    public static CrawlScheduler anInactiveScheduler() {
        LocalDateTime now = LocalDateTime.ofInstant(DEFAULT_CLOCK.now(), ZoneId.systemDefault());
        return CrawlScheduler.of(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.INACTIVE,
                now,
                now,
                DEFAULT_CLOCK);
    }

    /**
     * 할당된 ID를 가진 비활성 스케줄러 생성 (특정 Clock)
     *
     * @param clock 시간 제어
     * @return CrawlScheduler (ID = 1L, INACTIVE)
     */
    public static CrawlScheduler anInactiveScheduler(Clock clock) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        return CrawlScheduler.of(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.INACTIVE,
                now,
                now,
                clock);
    }

    private CrawlSchedulerFixture() {
        // Utility class
    }
}
