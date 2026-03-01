package com.ryuqq.crawlinghub.application.execution.internal;

import com.ryuqq.crawlinghub.application.common.metric.CrawlHubMetrics;
import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper.CrawlContextMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.CrawlResultProcessor;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.CrawlResultProcessorProvider;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.ProcessingResult;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.exception.RetryableExecutionException;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 실행 Coordinator
 *
 * <p><strong>책임</strong>: 도메인 상태 변경 + 크롤링 흐름 조율 (HikariCP try-finally 패턴)
 *
 * <ul>
 *   <li>도메인 상태 변경 후 Facade에 위임하여 @Transactional로 저장
 *   <li>크롤링 실행은 CrawlingProcessor에 위임 (비-트랜잭션)
 *   <li>전체 흐름: borrow → prepare → crawl → return (finally) → complete
 * </ul>
 *
 * <p><strong>트랜잭션 원칙</strong>: 이 클래스는 @Transactional을 직접 사용하지 않음. 도메인 상태를 변경한 뒤 Facade에 넘겨 트랜잭션으로
 * 묶어 저장합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskExecutionCoordinator {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskExecutionCoordinator.class);
    private static final String CYCLE_METRIC = "useragent_cycle_total";
    private static final String HTTP_STATUS_METRIC = "crawl_http_status_total";

    private final ExecutionCommandFacade commandFacade;
    private final CrawlingUserAgentCoordinator userAgentCoordinator;
    private final CrawlingProcessor crawlingProcessor;
    private final CrawlResultProcessorProvider processorProvider;
    private final FollowUpTaskCreator followUpTaskCreator;
    private final CrawlContextMapper crawlContextMapper;
    private final CrawlHubMetrics metrics;

    public CrawlTaskExecutionCoordinator(
            ExecutionCommandFacade commandFacade,
            CrawlingUserAgentCoordinator userAgentCoordinator,
            CrawlingProcessor crawlingProcessor,
            CrawlResultProcessorProvider processorProvider,
            FollowUpTaskCreator followUpTaskCreator,
            CrawlContextMapper crawlContextMapper,
            CrawlHubMetrics metrics) {
        this.commandFacade = commandFacade;
        this.userAgentCoordinator = userAgentCoordinator;
        this.crawlingProcessor = crawlingProcessor;
        this.processorProvider = processorProvider;
        this.followUpTaskCreator = followUpTaskCreator;
        this.crawlContextMapper = crawlContextMapper;
        this.metrics = metrics;
    }

    /**
     * CrawlTask 실행 전체 흐름 조율 (HikariCP try-finally 패턴)
     *
     * <ol>
     *   <li>borrow: UserAgent IDLE → BORROWED (Task 상태 변경 전, 실패 시 RetryableExecutionException)
     *   <li>prepareExecution: Task → RUNNING, Execution 생성 → Facade persist
     *   <li>crawlingProcessor: 크롤링 실행 (비-트랜잭션)
     *   <li>returnAgent: BORROWED → IDLE/COOLDOWN/SUSPENDED (finally 보장)
     *   <li>completeExecution: 결과에 따른 상태 업데이트 → Facade persist
     * </ol>
     *
     * @param bundle 초기 Bundle
     * @throws RetryableExecutionException UserAgent borrow 실패 시 (Task는 PUBLISHED 유지 → SQS 재시도 안전)
     */
    public void execute(CrawlTaskExecutionBundle bundle) {
        BorrowedUserAgent agent = borrowUserAgent(bundle);

        CrawlTaskExecutionBundle enrichedBundle = enrichBundle(bundle, agent);
        prepareExecution(enrichedBundle);

        boolean success = false;
        int httpStatusCode = 0;

        try {
            CrawlResult result = crawlingProcessor.executeCrawling(enrichedBundle);
            success = result.isSuccess();
            httpStatusCode = result.httpStatusCode() != null ? result.httpStatusCode() : 0;

            completeExecution(enrichedBundle, result);

            if (result.isSuccess()) {
                processResult(result, enrichedBundle.crawlTask());
            }
        } catch (Exception e) {
            safeCompleteWithFailure(enrichedBundle, e);
        } finally {
            userAgentCoordinator.returnAgent(
                    agent.userAgentId(), success, httpStatusCode, agent.consecutiveRateLimits());
            metrics.incrementCounter(CYCLE_METRIC, "outcome", success ? "success" : "failure");
            if (httpStatusCode > 0) {
                metrics.incrementCounterWithStatusCode(HTTP_STATUS_METRIC, httpStatusCode);
            }
        }
    }

    /**
     * UserAgent borrow (Task 상태 변경 전)
     *
     * <p>Circuit Breaker Open 또는 가용 UserAgent 부재 시 RetryableExecutionException을 던져 SQS 재시도를 유도합니다.
     * 이 시점에서 Task는 아직 PUBLISHED 상태이므로 재시도 시 정상 처리됩니다.
     *
     * @param bundle 초기 Bundle
     * @return BorrowedUserAgent
     * @throws RetryableExecutionException UserAgent borrow 실패 시
     */
    private BorrowedUserAgent borrowUserAgent(CrawlTaskExecutionBundle bundle) {
        try {
            return userAgentCoordinator.borrow();
        } catch (UserAgentException e) {
            throw new RetryableExecutionException(
                    "UserAgent borrow 실패 (SQS 재시도 대상): taskId="
                            + bundle.command().taskId()
                            + ", reason="
                            + e.getMessage(),
                    e);
        }
    }

    private CrawlTaskExecutionBundle enrichBundle(
            CrawlTaskExecutionBundle bundle, BorrowedUserAgent agent) {
        CrawlContext context = crawlContextMapper.toCrawlContext(bundle.crawlTask(), agent);
        return bundle.withCrawlContext(context);
    }

    /** 실행 준비: 도메인 상태 변경 → Facade persist */
    private void prepareExecution(CrawlTaskExecutionBundle bundle) {
        CrawlTask crawlTask = bundle.crawlTask();
        crawlTask.markAsRunning(bundle.changedAt());
        commandFacade.persist(bundle);

        log.info("CrawlTask 실행 준비 완료: taskId={}", bundle.command().taskId());
    }

    /** 실행 완료: 성공/실패에 따른 도메인 상태 변경 → Facade persist */
    private void completeExecution(CrawlTaskExecutionBundle bundle, CrawlResult result) {
        Instant now = Instant.now();
        CrawlExecution execution = bundle.execution();
        CrawlTask task = bundle.crawlTask();

        if (result.isSuccess()) {
            String responseSummary = buildResponseSummary(result.responseBody());
            execution.completeWithSuccess(responseSummary, result.httpStatusCode(), now);
            task.markAsSuccess(now);
            log.info(
                    "CrawlTask 실행 완료: taskId={}, durationMs={}",
                    task.getIdValue(),
                    execution.getDuration().durationMs());
        } else {
            execution.completeWithFailure(result.httpStatusCode(), result.errorMessage(), now);
            task.markAsFailed(now);
            log.warn(
                    "CrawlTask 크롤링 실패: taskId={}, httpStatus={}, error={}, durationMs={}",
                    task.getIdValue(),
                    result.httpStatusCode(),
                    result.errorMessage(),
                    execution.getDuration().durationMs());
        }

        commandFacade.persist(bundle);
    }

    /**
     * 안전한 실패 처리
     *
     * <p>예외 발생 시에도 전파하지 않습니다. RUNNING 고아 복구 스케줄러가 처리합니다.
     */
    private void safeCompleteWithFailure(CrawlTaskExecutionBundle bundle, Exception cause) {
        try {
            Instant now = Instant.now();
            bundle.execution().completeWithFailure(null, cause.getMessage(), now);
            bundle.crawlTask().markAsFailed(now);
            commandFacade.persist(bundle);

            log.error(
                    "CrawlTask 실행 실패: taskId={}, error={}",
                    bundle.crawlTask().getIdValue(),
                    cause.getMessage());
        } catch (Exception failureException) {
            log.error(
                    "실패 처리 중 예외 - RUNNING 고아 발생 가능: taskId={}, originalError={},"
                            + " failureError={}",
                    bundle.crawlTask().getIdValue(),
                    cause.getMessage(),
                    failureException.getMessage(),
                    failureException);
        }
    }

    /**
     * 응답 본문 요약 생성
     *
     * <p>전체 raw 응답(수백KB) 대신 크기 정보만 저장하여 DB 부하를 줄입니다. 파싱된 상품 데이터는 crawled_raw 테이블에 별도 저장됩니다.
     */
    private String buildResponseSummary(String responseBody) {
        if (responseBody == null) {
            return null;
        }
        return "{\"responseLengthChars\":" + responseBody.length() + "}";
    }

    private void processResult(CrawlResult crawlResult, CrawlTask crawlTask) {
        CrawlResultProcessor processor = processorProvider.getProcessor(crawlTask.getTaskType());
        ProcessingResult processingResult = processor.process(crawlResult, crawlTask);

        log.debug(
                "크롤링 결과 처리 완료: taskType={}, parsedItems={}, savedItems={}, followUpTasks={}",
                crawlTask.getTaskType(),
                processingResult.getParsedItemCount(),
                processingResult.getSavedItemCount(),
                processingResult.getFollowUpCommands().size());

        if (processingResult.hasFollowUpTasks()) {
            followUpTaskCreator.executeBatch(processingResult.getFollowUpCommands());
            log.info(
                    "후속 CrawlTask 생성 요청: taskType={}, count={}",
                    crawlTask.getTaskType(),
                    processingResult.getFollowUpCommands().size());
        }
    }
}
