package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.task.vo.RetryCount;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import java.time.LocalDateTime;

/**
 * CrawlTask Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskFixture {

    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    /**
     * 신규 CrawlTask 생성 (ID 미할당, WAITING 상태)
     *
     * @return CrawlTask (ID = null, WAITING)
     */
    public static CrawlTask aNewTask() {
        return CrawlTask.forNew(
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint());
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
                CrawlEndpointFixture.aMiniShopListEndpoint());
    }

    /**
     * ID가 할당된 WAITING 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, WAITING)
     */
    public static CrawlTask aWaitingTask() {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
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
     * PUBLISHED 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, PUBLISHED)
     */
    public static CrawlTask aPublishedTask() {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.PUBLISHED,
                RetryCountFixture.zero(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * RUNNING 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, RUNNING)
     */
    public static CrawlTask aRunningTask() {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.RUNNING,
                RetryCountFixture.zero(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * SUCCESS 상태 CrawlTask 생성
     *
     * @return CrawlTask (ID = 1L, SUCCESS)
     */
    public static CrawlTask aSuccessTask() {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.SUCCESS,
                RetryCountFixture.zero(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
    }

    /**
     * FAILED 상태 CrawlTask 생성 (재시도 가능)
     *
     * @return CrawlTask (ID = 1L, FAILED, retryCount = 0)
     */
    public static CrawlTask aFailedTask() {
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.FAILED,
                RetryCountFixture.zero(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
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
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskTypeFixture.defaultType(),
                CrawlEndpointFixture.aMiniShopListEndpoint(),
                CrawlTaskStatus.TIMEOUT,
                RetryCountFixture.zero(),
                null,
                DEFAULT_TIME,
                DEFAULT_TIME);
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

    private CrawlTaskFixture() {
        // Utility class
    }
}
