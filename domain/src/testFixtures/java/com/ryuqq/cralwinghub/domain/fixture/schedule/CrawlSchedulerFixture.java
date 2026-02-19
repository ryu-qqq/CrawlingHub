package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;

/**
 * CrawlScheduler Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlSchedulerFixture {

    private static final Instant DEFAULT_INSTANT = FixedClock.aDefaultClock().instant();

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
                DEFAULT_INSTANT);
    }

    /**
     * 신규 활성 스케줄러 생성 (특정 시간)
     *
     * @param now 현재 시간
     * @return 신규 CrawlScheduler (ID = null, ACTIVE)
     */
    public static CrawlScheduler aNewActiveScheduler(Instant now) {
        return CrawlScheduler.forNew(
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                now);
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
                DEFAULT_INSTANT);
    }

    /**
     * 할당된 ID를 가진 활성 스케줄러 생성
     *
     * @return CrawlScheduler (ID = 1L, ACTIVE)
     */
    public static CrawlScheduler anActiveScheduler() {
        return CrawlScheduler.reconstitute(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.ACTIVE,
                DEFAULT_INSTANT,
                DEFAULT_INSTANT);
    }

    /**
     * 할당된 ID를 가진 활성 스케줄러 생성 (특정 ID)
     *
     * @param id 스케줄러 ID
     * @return CrawlScheduler (ACTIVE)
     */
    public static CrawlScheduler anActiveScheduler(Long id) {
        return CrawlScheduler.reconstitute(
                CrawlSchedulerId.of(id),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.ACTIVE,
                DEFAULT_INSTANT,
                DEFAULT_INSTANT);
    }

    /**
     * 할당된 ID를 가진 활성 스케줄러 생성 (특정 시간)
     *
     * @param now 현재 시간
     * @return CrawlScheduler (ID = 1L, ACTIVE)
     */
    public static CrawlScheduler anActiveScheduler(Instant now) {
        return CrawlScheduler.reconstitute(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.ACTIVE,
                now,
                now);
    }

    /**
     * 할당된 ID를 가진 비활성 스케줄러 생성
     *
     * @return CrawlScheduler (ID = 1L, INACTIVE)
     */
    public static CrawlScheduler anInactiveScheduler() {
        return CrawlScheduler.reconstitute(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.INACTIVE,
                DEFAULT_INSTANT,
                DEFAULT_INSTANT);
    }

    /**
     * 할당된 ID를 가진 비활성 스케줄러 생성 (특정 시간)
     *
     * @param now 현재 시간
     * @return CrawlScheduler (ID = 1L, INACTIVE)
     */
    public static CrawlScheduler anInactiveScheduler(Instant now) {
        return CrawlScheduler.reconstitute(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                SchedulerNameFixture.aDefaultName(),
                CronExpressionFixture.aDefaultCron(),
                SchedulerStatus.INACTIVE,
                now,
                now);
    }

    private CrawlSchedulerFixture() {
        // Utility class
    }
}
