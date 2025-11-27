package com.ryuqq.crawlinghub.application.execution.manager;

import com.ryuqq.crawlinghub.application.execution.port.out.command.CrawlExecutionPersistencePort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawlExecution Manager
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawlExecution 생성 및 저장
 *   <li>CrawlExecution 상태 전환 (성공/실패/타임아웃)
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: 이 클래스는 트랜잭션을 직접 관리하지 않음. 호출자(Facade)에서 트랜잭션 경계를 관리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlExecutionManager {

    private static final Logger log = LoggerFactory.getLogger(CrawlExecutionManager.class);

    private final CrawlExecutionPersistencePort crawlExecutionPersistencePort;

    public CrawlExecutionManager(CrawlExecutionPersistencePort crawlExecutionPersistencePort) {
        this.crawlExecutionPersistencePort = crawlExecutionPersistencePort;
    }

    /**
     * CrawlExecution 시작 및 저장 (RUNNING 상태로 생성 후 즉시 저장)
     *
     * <p>실행 이력 추적을 위해 RUNNING 상태로 먼저 저장합니다. 크롤링 중 서버가 죽어도 실행 이력이 남습니다.
     *
     * @param crawlTaskId CrawlTask ID
     * @param crawlSchedulerId CrawlScheduler ID
     * @param sellerId Seller ID
     * @return 저장된 CrawlExecution (ID 할당됨)
     */
    public CrawlExecution startAndPersist(
            CrawlTaskId crawlTaskId, CrawlSchedulerId crawlSchedulerId, SellerId sellerId) {
        log.debug(
                "CrawlExecution 시작: taskId={}, schedulerId={}",
                crawlTaskId.value(),
                crawlSchedulerId.value());

        CrawlExecution execution = CrawlExecution.start(crawlTaskId, crawlSchedulerId, sellerId);
        CrawlExecutionId savedId = crawlExecutionPersistencePort.persist(execution);

        log.info(
                "CrawlExecution 생성 및 저장: executionId={}, taskId={}, status=RUNNING",
                savedId.value(),
                crawlTaskId.value());

        return execution;
    }

    /**
     * CrawlExecution 성공 완료 및 저장
     *
     * @param execution 완료할 CrawlExecution
     * @param responseBody 응답 본문
     * @param httpStatusCode HTTP 상태 코드
     * @return 저장된 CrawlExecution ID
     */
    public CrawlExecutionId completeWithSuccess(
            CrawlExecution execution, String responseBody, Integer httpStatusCode) {
        execution.completeWithSuccess(responseBody, httpStatusCode);

        CrawlExecutionId savedId = crawlExecutionPersistencePort.persist(execution);

        log.info(
                "CrawlExecution 성공 완료: executionId={}, taskId={}, durationMs={}",
                savedId.value(),
                execution.getCrawlTaskId().value(),
                execution.getDuration().durationMs());

        return savedId;
    }

    /**
     * CrawlExecution 실패 완료 및 저장
     *
     * @param execution 완료할 CrawlExecution
     * @param httpStatusCode HTTP 상태 코드 (nullable)
     * @param errorMessage 에러 메시지
     * @return 저장된 CrawlExecution ID
     */
    public CrawlExecutionId completeWithFailure(
            CrawlExecution execution, Integer httpStatusCode, String errorMessage) {
        execution.completeWithFailure(httpStatusCode, errorMessage);

        CrawlExecutionId savedId = crawlExecutionPersistencePort.persist(execution);

        log.warn(
                "CrawlExecution 실패 완료: executionId={}, taskId={}, httpStatus={}, error={},"
                        + " durationMs={}",
                savedId.value(),
                execution.getCrawlTaskId().value(),
                httpStatusCode,
                errorMessage,
                execution.getDuration().durationMs());

        return savedId;
    }

    /**
     * CrawlExecution 실패 완료 및 저장 (응답 본문 포함)
     *
     * @param execution 완료할 CrawlExecution
     * @param responseBody 에러 응답 본문
     * @param httpStatusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지
     * @return 저장된 CrawlExecution ID
     */
    public CrawlExecutionId completeWithFailure(
            CrawlExecution execution,
            String responseBody,
            Integer httpStatusCode,
            String errorMessage) {
        execution.completeWithFailure(responseBody, httpStatusCode, errorMessage);

        CrawlExecutionId savedId = crawlExecutionPersistencePort.persist(execution);

        log.warn(
                "CrawlExecution 실패 완료: executionId={}, taskId={}, httpStatus={}, error={},"
                        + " durationMs={}",
                savedId.value(),
                execution.getCrawlTaskId().value(),
                httpStatusCode,
                errorMessage,
                execution.getDuration().durationMs());

        return savedId;
    }

    /**
     * CrawlExecution 타임아웃 완료 및 저장
     *
     * @param execution 완료할 CrawlExecution
     * @param errorMessage 타임아웃 에러 메시지
     * @return 저장된 CrawlExecution ID
     */
    public CrawlExecutionId completeWithTimeout(CrawlExecution execution, String errorMessage) {
        execution.completeWithTimeout(errorMessage);

        CrawlExecutionId savedId = crawlExecutionPersistencePort.persist(execution);

        log.warn(
                "CrawlExecution 타임아웃: executionId={}, taskId={}, error={}, durationMs={}",
                savedId.value(),
                execution.getCrawlTaskId().value(),
                errorMessage,
                execution.getDuration().durationMs());

        return savedId;
    }
}
