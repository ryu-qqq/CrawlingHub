package com.ryuqq.cralwinghub.domain.fixture.execution;

import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import java.time.LocalDateTime;

/**
 * CrawlExecution Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlExecutionFixture {

    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(2025, 11, 27, 12, 0, 0);

    /**
     * 신규 실행 시작 (ID 미할당, RUNNING 상태)
     *
     * @return CrawlExecution (ID = null, RUNNING)
     */
    public static CrawlExecution aNewExecution() {
        return CrawlExecution.start(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId());
    }

    /**
     * ID가 할당된 RUNNING 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, RUNNING)
     */
    public static CrawlExecution aRunningExecution() {
        return CrawlExecution.reconstitute(
                CrawlExecutionIdFixture.anAssignedId(),
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlExecutionStatus.RUNNING,
                CrawlExecutionResultFixture.empty(),
                ExecutionDurationFixture.aRunningDuration(),
                DEFAULT_TIME);
    }

    /**
     * SUCCESS 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, SUCCESS)
     */
    public static CrawlExecution aSuccessExecution() {
        return CrawlExecution.reconstitute(
                CrawlExecutionIdFixture.anAssignedId(),
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlExecutionStatus.SUCCESS,
                CrawlExecutionResultFixture.aSuccessResult(),
                ExecutionDurationFixture.aCompletedDuration(),
                DEFAULT_TIME);
    }

    /**
     * FAILED 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, FAILED)
     */
    public static CrawlExecution aFailedExecution() {
        return CrawlExecution.reconstitute(
                CrawlExecutionIdFixture.anAssignedId(),
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlExecutionStatus.FAILED,
                CrawlExecutionResultFixture.aFailureResult(),
                ExecutionDurationFixture.aCompletedDuration(),
                DEFAULT_TIME);
    }

    /**
     * TIMEOUT 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, TIMEOUT)
     */
    public static CrawlExecution aTimeoutExecution() {
        return CrawlExecution.reconstitute(
                CrawlExecutionIdFixture.anAssignedId(),
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlExecutionStatus.TIMEOUT,
                CrawlExecutionResultFixture.aTimeoutResult(),
                ExecutionDurationFixture.aCompletedDuration(),
                DEFAULT_TIME);
    }

    /**
     * Rate Limited 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, FAILED, HTTP 429)
     */
    public static CrawlExecution aRateLimitedExecution() {
        return CrawlExecution.reconstitute(
                CrawlExecutionIdFixture.anAssignedId(),
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlExecutionStatus.FAILED,
                CrawlExecutionResultFixture.aRateLimitedResult(),
                ExecutionDurationFixture.aCompletedDuration(),
                DEFAULT_TIME);
    }

    /**
     * 특정 ID를 가진 CrawlExecution 생성
     *
     * @param id CrawlExecution ID
     * @return CrawlExecution (RUNNING)
     */
    public static CrawlExecution anExecutionWithId(Long id) {
        return CrawlExecution.reconstitute(
                CrawlExecutionIdFixture.anAssignedId(id),
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlExecutionStatus.RUNNING,
                CrawlExecutionResultFixture.empty(),
                ExecutionDurationFixture.aRunningDuration(),
                DEFAULT_TIME);
    }

    private CrawlExecutionFixture() {
        // Utility class
    }
}
