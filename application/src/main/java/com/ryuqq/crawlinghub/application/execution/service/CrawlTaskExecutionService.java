package com.ryuqq.crawlinghub.application.execution.service;

import com.ryuqq.crawlinghub.application.crawl.component.Crawler;
import com.ryuqq.crawlinghub.application.crawl.component.CrawlerProvider;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlContext;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.execution.dto.ExecutionContext;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.facade.CrawlTaskExecutionFacade;
import com.ryuqq.crawlinghub.application.execution.port.in.CrawlTaskExecutionUseCase;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.ConsumeUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecordUserAgentResultUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 실행 Service
 *
 * <p><strong>용도</strong>: SQS에서 수신한 CrawlTask 메시지를 처리하여 크롤링 실행
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Facade.prepareExecution: CrawlTask 조회 → RUNNING 상태 → CrawlExecution 생성
 *   <li>ConsumeUserAgentUseCase: Redis Pool에서 사용 가능한 UserAgent 선택
 *   <li>CrawlerProvider를 통해 TaskType에 맞는 Crawler 선택 → 크롤링 실행
 *   <li>RecordUserAgentResultUseCase: UserAgent 결과 기록 (성공/실패/429)
 *   <li>Facade.completeWithSuccess/Failure: 결과에 따른 상태 업데이트
 * </ol>
 *
 * <p><strong>트랜잭션 경계</strong>:
 *
 * <ul>
 *   <li>prepareExecution: 트랜잭션 내 (DB 상태 변경)
 *   <li>consumeUserAgent: 트랜잭션 외부 (Redis 조회)
 *   <li>executeCrawling: 트랜잭션 외부 (HTTP 호출)
 *   <li>recordUserAgentResult: 트랜잭션 외부 (Redis 업데이트)
 *   <li>completeWith*: 트랜잭션 내 (DB 상태 변경)
 * </ul>
 *
 * <p><strong>예외 처리</strong>:
 *
 * <ul>
 *   <li>CrawlTask 미존재 → RuntimeException → DLQ
 *   <li>UserAgent 없음 → NoAvailableUserAgentException → DLQ
 *   <li>크롤링 실패 (429 등) → UserAgent 결과 기록 후 DB 상태 업데이트
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class CrawlTaskExecutionService implements CrawlTaskExecutionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskExecutionService.class);

    private final CrawlTaskExecutionFacade crawlTaskExecutionFacade;
    private final CrawlerProvider crawlerProvider;
    private final ConsumeUserAgentUseCase consumeUserAgentUseCase;
    private final RecordUserAgentResultUseCase recordUserAgentResultUseCase;

    public CrawlTaskExecutionService(
            CrawlTaskExecutionFacade crawlTaskExecutionFacade,
            CrawlerProvider crawlerProvider,
            ConsumeUserAgentUseCase consumeUserAgentUseCase,
            RecordUserAgentResultUseCase recordUserAgentResultUseCase) {
        this.crawlTaskExecutionFacade = crawlTaskExecutionFacade;
        this.crawlerProvider = crawlerProvider;
        this.consumeUserAgentUseCase = consumeUserAgentUseCase;
        this.recordUserAgentResultUseCase = recordUserAgentResultUseCase;
    }

    @Override
    public void execute(ExecuteCrawlTaskCommand command) {
        Long taskId = command.taskId();

        log.info(
                "CrawlTask 실행 시작: taskId={}, schedulerId={}, taskType={}",
                taskId,
                command.schedulerId(),
                command.taskType());

        // 1. 실행 준비 (트랜잭션 내: CrawlTask 조회 → RUNNING → CrawlExecution 생성)
        ExecutionContext context = crawlTaskExecutionFacade.prepareExecution(command);

        // 2. UserAgent 토큰 소비 (트랜잭션 외부: Redis Pool에서 선택)
        CachedUserAgent userAgent = consumeUserAgentUseCase.execute();
        log.debug(
                "UserAgent 선택 완료: userAgentId={}, remainingTokens={}",
                userAgent.userAgentId(),
                userAgent.remainingTokens());

        try {
            // 3. 크롤링 실행 (트랜잭션 외부: HTTP 호출)
            CrawlResult crawlResult = executeCrawling(context, userAgent);

            // 4. UserAgent 결과 기록 (트랜잭션 외부: Redis 업데이트)
            recordUserAgentResult(userAgent, crawlResult);

            if (crawlResult.isSuccess()) {
                // 5-1. 성공 처리 (트랜잭션 내: CrawlExecution + CrawlTask 상태 업데이트 + 결과 처리)
                crawlTaskExecutionFacade.completeWithSuccess(context, crawlResult);
                log.info(
                        "CrawlTask 실행 완료: taskId={}, userAgentId={}",
                        taskId,
                        userAgent.userAgentId());
            } else {
                // 5-2. 크롤링 실패 처리
                crawlTaskExecutionFacade.completeWithFailure(
                        context, crawlResult.getHttpStatusCode(), crawlResult.getErrorMessage());
                log.warn(
                        "CrawlTask 크롤링 실패: taskId={}, userAgentId={}, httpStatus={}, error={}",
                        taskId,
                        userAgent.userAgentId(),
                        crawlResult.getHttpStatusCode(),
                        crawlResult.getErrorMessage());
            }

        } catch (RuntimeException e) {
            // 예외 발생 시 UserAgent 실패 기록
            recordUserAgentResultUseCase.execute(
                    RecordUserAgentResultCommand.failure(userAgent.userAgentId(), 0));

            // 5-3. 예외 발생 시 실패 처리 (트랜잭션 내: CrawlExecution + CrawlTask 상태 업데이트)
            crawlTaskExecutionFacade.completeWithFailure(context, null, e.getMessage());

            log.error(
                    "CrawlTask 실행 실패: taskId={}, userAgentId={}, error={}",
                    taskId,
                    userAgent.userAgentId(),
                    e.getMessage());

            // DLQ 처리를 위해 예외 재전파
            throw e;
        }
    }

    /**
     * 실제 크롤링 수행
     *
     * <p>CrawlerProvider를 통해 TaskType에 맞는 Crawler를 선택하여 크롤링 실행
     *
     * <p><strong>중요</strong>: 이 메서드는 트랜잭션 외부에서 실행됩니다. HTTP 호출이므로 @Transactional 내에서 실행하면 안 됩니다.
     *
     * @param executionContext 실행 컨텍스트 (CrawlTask 포함)
     * @param userAgent 사용할 UserAgent 정보
     * @return 크롤링 결과
     */
    private CrawlResult executeCrawling(
            ExecutionContext executionContext, CachedUserAgent userAgent) {
        CrawlContext crawlContext = CrawlContext.of(executionContext.crawlTask(), userAgent);

        log.debug(
                "크롤링 실행: taskType={}, endpoint={}, userAgentId={}",
                crawlContext.getTaskType(),
                crawlContext.getEndpoint(),
                crawlContext.getUserAgentId());

        Crawler crawler = crawlerProvider.getCrawler(crawlContext.getTaskType());
        return crawler.crawl(crawlContext);
    }

    /**
     * UserAgent 결과 기록
     *
     * <p>크롤링 결과에 따라 UserAgent의 Health Score를 업데이트합니다.
     *
     * <p><strong>처리 규칙</strong>:
     *
     * <ul>
     *   <li>성공: Health Score +5
     *   <li>429 응답: 즉시 SUSPENDED (Pool에서 제거)
     *   <li>기타 에러: Health Score 감소
     * </ul>
     *
     * @param userAgent 사용한 UserAgent
     * @param crawlResult 크롤링 결과
     */
    private void recordUserAgentResult(CachedUserAgent userAgent, CrawlResult crawlResult) {
        RecordUserAgentResultCommand command;

        if (crawlResult.isSuccess()) {
            command = RecordUserAgentResultCommand.success(userAgent.userAgentId());
        } else {
            command =
                    RecordUserAgentResultCommand.failure(
                            userAgent.userAgentId(), crawlResult.getHttpStatusCode());
        }

        recordUserAgentResultUseCase.execute(command);
    }
}
