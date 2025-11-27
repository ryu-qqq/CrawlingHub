package com.ryuqq.crawlinghub.domain.execution.aggregate;

import com.ryuqq.crawlinghub.domain.execution.exception.InvalidCrawlExecutionStateException;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionResult;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.vo.ExecutionDuration;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.time.LocalDateTime;

/**
 * CrawlExecution Aggregate Root
 *
 * <p>크롤링 실행 이력을 관리하는 Aggregate Root
 *
 * <p><strong>용도</strong>:
 *
 * <ul>
 *   <li>CrawlTask의 각 실행 시도를 개별적으로 기록
 *   <li>스케줄러별/태스크별 실행 통계 산출
 *   <li>실행 결과 및 에러 로그 보관
 * </ul>
 *
 * <p><strong>생명주기</strong>:
 *
 * <pre>
 * 1. CrawlExecution.start() - RUNNING 상태로 생성
 * 2-a. completeWithSuccess() - SUCCESS 상태로 전환
 * 2-b. completeWithFailure() - FAILED 상태로 전환
 * 2-c. completeWithTimeout() - TIMEOUT 상태로 전환
 * </pre>
 *
 * <p><strong>CrawlTask와의 관계</strong>:
 *
 * <ul>
 *   <li>CrawlTask 1 : N CrawlExecution (재시도 시 새 Execution 생성)
 *   <li>Long FK로 참조 (JPA 관계 어노테이션 사용 안함)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlExecution {

    private final CrawlExecutionId id;
    private final CrawlTaskId crawlTaskId;
    private final CrawlSchedulerId crawlSchedulerId;
    private final SellerId sellerId;
    private CrawlExecutionStatus status;
    private CrawlExecutionResult result;
    private ExecutionDuration duration;
    private final LocalDateTime createdAt;

    private CrawlExecution(
            CrawlExecutionId id,
            CrawlTaskId crawlTaskId,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            CrawlExecutionStatus status,
            CrawlExecutionResult result,
            ExecutionDuration duration,
            LocalDateTime createdAt) {
        this.id = id;
        this.crawlTaskId = crawlTaskId;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.status = status;
        this.result = result;
        this.duration = duration;
        this.createdAt = createdAt;
    }

    /**
     * 새로운 CrawlExecution 시작
     *
     * <p>RUNNING 상태로 생성됩니다.
     *
     * @param crawlTaskId CrawlTask ID
     * @param crawlSchedulerId CrawlScheduler ID
     * @param sellerId Seller ID
     * @return 새로운 CrawlExecution (RUNNING 상태)
     */
    public static CrawlExecution start(
            CrawlTaskId crawlTaskId, CrawlSchedulerId crawlSchedulerId, SellerId sellerId) {
        return new CrawlExecution(
                CrawlExecutionId.unassigned(),
                crawlTaskId,
                crawlSchedulerId,
                sellerId,
                CrawlExecutionStatus.RUNNING,
                CrawlExecutionResult.empty(),
                ExecutionDuration.start(),
                LocalDateTime.now());
    }

    /**
     * 기존 데이터로 CrawlExecution 복원 (영속성 계층 전용)
     *
     * @param id CrawlExecution ID
     * @param crawlTaskId CrawlTask ID
     * @param crawlSchedulerId CrawlScheduler ID
     * @param sellerId Seller ID
     * @param status 현재 상태
     * @param result 실행 결과
     * @param duration 실행 시간
     * @param createdAt 생성 시각
     * @return 복원된 CrawlExecution
     */
    public static CrawlExecution reconstitute(
            CrawlExecutionId id,
            CrawlTaskId crawlTaskId,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            CrawlExecutionStatus status,
            CrawlExecutionResult result,
            ExecutionDuration duration,
            LocalDateTime createdAt) {
        return new CrawlExecution(
                id, crawlTaskId, crawlSchedulerId, sellerId, status, result, duration, createdAt);
    }

    /**
     * 성공으로 완료
     *
     * @param responseBody 응답 본문
     * @param httpStatusCode HTTP 상태 코드
     * @throws InvalidCrawlExecutionStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void completeWithSuccess(String responseBody, Integer httpStatusCode) {
        validateRunningStatus();
        this.status = CrawlExecutionStatus.SUCCESS;
        this.result = CrawlExecutionResult.success(responseBody, httpStatusCode);
        this.duration = this.duration.complete();
    }

    /**
     * 실패로 완료
     *
     * @param httpStatusCode HTTP 상태 코드 (nullable)
     * @param errorMessage 에러 메시지
     * @throws InvalidCrawlExecutionStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void completeWithFailure(Integer httpStatusCode, String errorMessage) {
        validateRunningStatus();
        this.status = CrawlExecutionStatus.FAILED;
        this.result = CrawlExecutionResult.failure(httpStatusCode, errorMessage);
        this.duration = this.duration.complete();
    }

    /**
     * 실패로 완료 (응답 본문 포함)
     *
     * @param responseBody 에러 응답 본문
     * @param httpStatusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지
     * @throws InvalidCrawlExecutionStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void completeWithFailure(
            String responseBody, Integer httpStatusCode, String errorMessage) {
        validateRunningStatus();
        this.status = CrawlExecutionStatus.FAILED;
        this.result =
                CrawlExecutionResult.failureWithBody(responseBody, httpStatusCode, errorMessage);
        this.duration = this.duration.complete();
    }

    /**
     * 타임아웃으로 완료
     *
     * @param errorMessage 타임아웃 에러 메시지
     * @throws InvalidCrawlExecutionStateException 현재 상태가 RUNNING이 아닌 경우
     */
    public void completeWithTimeout(String errorMessage) {
        validateRunningStatus();
        this.status = CrawlExecutionStatus.TIMEOUT;
        this.result = CrawlExecutionResult.timeout(errorMessage);
        this.duration = this.duration.complete();
    }

    /**
     * RUNNING 상태 검증
     *
     * @throws InvalidCrawlExecutionStateException RUNNING 상태가 아닌 경우
     */
    private void validateRunningStatus() {
        if (this.status != CrawlExecutionStatus.RUNNING) {
            throw new InvalidCrawlExecutionStateException(this.status);
        }
    }

    // === 상태 확인 메서드 ===

    /**
     * 실행 중 여부 확인
     *
     * @return RUNNING 상태면 true
     */
    public boolean isRunning() {
        return this.status == CrawlExecutionStatus.RUNNING;
    }

    /**
     * 성공 여부 확인
     *
     * @return SUCCESS 상태면 true
     */
    public boolean isSuccess() {
        return this.status.isSuccess();
    }

    /**
     * 실패 여부 확인
     *
     * @return FAILED 또는 TIMEOUT이면 true
     */
    public boolean isFailure() {
        return this.status.isFailure();
    }

    /**
     * 완료 여부 확인
     *
     * @return 종료 상태면 true
     */
    public boolean isCompleted() {
        return this.status.isTerminal();
    }

    /**
     * Rate Limit 에러 여부 확인
     *
     * @return HTTP 429면 true
     */
    public boolean isRateLimited() {
        return this.result != null && this.result.isRateLimited();
    }

    // === Getters ===

    public CrawlExecutionId getId() {
        return id;
    }

    public CrawlTaskId getCrawlTaskId() {
        return crawlTaskId;
    }

    public CrawlSchedulerId getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public CrawlExecutionStatus getStatus() {
        return status;
    }

    public CrawlExecutionResult getResult() {
        return result;
    }

    public ExecutionDuration getDuration() {
        return duration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
