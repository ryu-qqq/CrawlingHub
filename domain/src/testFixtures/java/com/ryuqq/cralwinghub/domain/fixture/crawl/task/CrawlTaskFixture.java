package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.time.Instant;

/**
 * CrawlTask Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskFixture {

    private static final Instant DEFAULT_INSTANT = FixedClock.aDefaultClock().instant();
    private static final Instant DEFAULT_TIME = DEFAULT_INSTANT;

    /**
     * 신규 CrawlTask 생성 (ID 미할당, WAITING 상태)
     *
     * @return CrawlTask (ID = null, WAITING)
     */
    public static CrawlTask forNew() {
        return CrawlTask.forNew(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                DEFAULT_INSTANT);
    }

    /**
     * 신규 CrawlTask 생성 (시간 지정)
     *
     * @param now 현재 시간
     * @return CrawlTask (ID = null, WAITING)
     */
    public static CrawlTask forNew(Instant now) {
        return CrawlTask.forNew(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                now);
    }

    /**
     * 신규 CrawlTask 생성 (ID 미할당, WAITING 상태)
     *
     * @return CrawlTask (ID = null, WAITING)
     * @deprecated Use {@link #forNew()} instead
     */
    @Deprecated
    public static CrawlTask aNewTask() {
        return forNew();
    }

    /**
     * 신규 CrawlTask 생성 (특정 셀러)
     *
     * @param sellerId 셀러 ID
     * @return CrawlTask (ID = null, WAITING)
     */
    public static CrawlTask aNewTask(SellerId sellerId) {
        return CrawlTask.forNew(
                CrawlSchedulerIdFixture.anAssignedId(),
                sellerId,
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                DEFAULT_INSTANT);
    }

    /**
     * ID가 할당된 WAITING 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, WAITING)
     */
    public static CrawlTask aWaitingTask() {
        return reconstitute(CrawlTaskStatus.WAITING);
    }

    /**
     * PUBLISHED 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, PUBLISHED)
     */
    public static CrawlTask aPublishedTask() {
        return reconstitute(CrawlTaskStatus.PUBLISHED);
    }

    /**
     * RUNNING 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, RUNNING)
     */
    public static CrawlTask aRunningTask() {
        return reconstitute(CrawlTaskStatus.RUNNING);
    }

    /**
     * SUCCESS 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, SUCCESS)
     */
    public static CrawlTask aSuccessTask() {
        return reconstitute(CrawlTaskStatus.SUCCESS);
    }

    /**
     * FAILED 상태 CrawlTask 생성 (재시도 가능)
     *
     * @return CrawlTask (ID = 1L, FAILED, retryCount = 0)
     */
    public static CrawlTask aFailedTask() {
        return reconstitute(CrawlTaskStatus.FAILED);
    }

    /**
     * FAILED 상태 CrawlTask 생성 (최대 재시도 도달)
     *
     * @return CrawlTask (ID = 1L, FAILED, retryCount = 2)
     */
    public static CrawlTask aFailedTaskWithMaxRetry() {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.FAILED,
                RetryCountFixture.maxRetry(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * TIMEOUT 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, TIMEOUT)
     */
    public static CrawlTask aTimeoutTask() {
        return reconstitute(CrawlTaskStatus.TIMEOUT);
    }

    /**
     * RETRY 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, RETRY)
     */
    public static CrawlTask aRetryTask() {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.RETRY,
                RetryCountFixture.one(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 특정 상태의 CrawlTask 생성
     *
     * @param status CrawlTask 상태
     * @return CrawlTask
     */
    public static CrawlTask aTaskWithStatus(CrawlTaskStatus status) {
        return reconstitute(status);
    }

    /**
     * 특정 ID를 가진 CrawlTask 생성
     *
     * @param id CrawlTask ID
     * @return CrawlTask (WAITING)
     */
    public static CrawlTask aTaskWithId(Long id) {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(id),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.WAITING,
                RetryCountFixture.zero(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * 영속성 복원용 Fixture (헬퍼 메서드)
     *
     * @param status 상태
     * @return CrawlTask
     */
    public static CrawlTask reconstitute(CrawlTaskStatus status) {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                status,
                RetryCountFixture.zero(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    private CrawlTaskFixture() {
        // Utility class
    }
}
