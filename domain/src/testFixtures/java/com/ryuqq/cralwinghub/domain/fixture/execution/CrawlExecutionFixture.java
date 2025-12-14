package com.ryuqq.cralwinghub.domain.fixture.execution;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Clock;
import java.time.Instant;

/**
 * CrawlExecution Aggregate Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlExecutionFixture {

    private static final Clock DEFAULT_CLOCK = FixedClock.aDefaultClock();
    private static final Instant DEFAULT_TIME = DEFAULT_CLOCK.instant();

    /**
     * 신규 실행 시작 (ID 미할당, RUNNING 상태)
     *
     * @return CrawlExecution (ID = null, RUNNING)
     */
    public static CrawlExecution forNew() {
        return CrawlExecution.start(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                DEFAULT_CLOCK);
    }

    /**
     * 신규 실행 시작 (Clock 지정)
     *
     * @param clock 시간 제어
     * @return CrawlExecution (ID = null, RUNNING)
     */
    public static CrawlExecution forNew(Clock clock) {
        return CrawlExecution.start(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                clock);
    }

    /**
     * 신규 실행 시작 (ID 미할당, RUNNING 상태)
     *
     * @return CrawlExecution (ID = null, RUNNING)
     * @deprecated Use {@link #forNew()} instead
     */
    @Deprecated
    public static CrawlExecution aNewExecution() {
        return forNew();
    }

    /**
     * ID가 할당된 RUNNING 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, RUNNING)
     */
    public static CrawlExecution aRunningExecution() {
        return reconstitute(
                CrawlExecutionStatus.RUNNING,
                CrawlExecutionResultFixture.empty(),
                ExecutionDurationFixture.aRunningDuration());
    }

    /**
     * SUCCESS 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, SUCCESS)
     */
    public static CrawlExecution aSuccessExecution() {
        return reconstitute(
                CrawlExecutionStatus.SUCCESS,
                CrawlExecutionResultFixture.aSuccessResult(),
                ExecutionDurationFixture.aCompletedDuration());
    }

    /**
     * FAILED 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, FAILED)
     */
    public static CrawlExecution aFailedExecution() {
        return reconstitute(
                CrawlExecutionStatus.FAILED,
                CrawlExecutionResultFixture.aFailureResult(),
                ExecutionDurationFixture.aCompletedDuration());
    }

    /**
     * TIMEOUT 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, TIMEOUT)
     */
    public static CrawlExecution aTimeoutExecution() {
        return reconstitute(
                CrawlExecutionStatus.TIMEOUT,
                CrawlExecutionResultFixture.aTimeoutResult(),
                ExecutionDurationFixture.aCompletedDuration());
    }

    /**
     * Rate Limited 상태 CrawlExecution 생성
     *
     * @return CrawlExecution (ID = 1L, FAILED, HTTP 429)
     */
    public static CrawlExecution aRateLimitedExecution() {
        return reconstitute(
                CrawlExecutionStatus.FAILED,
                CrawlExecutionResultFixture.aRateLimitedResult(),
                ExecutionDurationFixture.aCompletedDuration());
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

    /**
     * 영속성 복원용 Fixture (헬퍼 메서드)
     *
     * @param status 상태
     * @param result 결과
     * @param duration 실행 시간
     * @return CrawlExecution
     */
    public static CrawlExecution reconstitute(
            CrawlExecutionStatus status,
            com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionResult result,
            com.ryuqq.crawlinghub.domain.execution.vo.ExecutionDuration duration) {
        return CrawlExecution.reconstitute(
                CrawlExecutionIdFixture.anAssignedId(),
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                status,
                result,
                duration,
                DEFAULT_TIME);
    }

    private CrawlExecutionFixture() {
        // Utility class
    }
}
