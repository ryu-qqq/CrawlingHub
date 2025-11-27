package com.ryuqq.crawlinghub.application.execution.facade;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.processor.CrawlResultProcessor;
import com.ryuqq.crawlinghub.application.crawl.processor.CrawlResultProcessorProvider;
import com.ryuqq.crawlinghub.application.crawl.processor.ProcessingResult;
import com.ryuqq.crawlinghub.application.execution.dto.ExecutionContext;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.manager.CrawlExecutionManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.CreateCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlTask 실행 Facade
 *
 * <p><strong>책임</strong>: CrawlTask 실행 흐름 조율
 *
 * <ul>
 *   <li>CrawlTask 상태 전환 (PUBLISHED → RUNNING → SUCCESS/FAILED)
 *   <li>CrawlExecution 생성 및 완료 처리
 *   <li>각 Manager를 통한 상태 관리 위임
 * </ul>
 *
 * <p><strong>트랜잭션 경계</strong>: 이 클래스에서 트랜잭션을 관리합니다.
 *
 * <p><strong>주의</strong>: 실제 크롤링 실행(HTTP 호출)은 이 클래스에서 수행하지 않습니다. 크롤링 실행은 {@code @Transactional}
 * 외부에서 수행되어야 합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskExecutionFacade {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskExecutionFacade.class);

    private final CrawlTaskQueryPort crawlTaskQueryPort;
    private final CrawlTaskTransactionManager crawlTaskTransactionManager;
    private final CrawlExecutionManager crawlExecutionManager;
    private final CrawlResultProcessorProvider processorProvider;
    private final CreateCrawlTaskUseCase createCrawlTaskUseCase;

    public CrawlTaskExecutionFacade(
            CrawlTaskQueryPort crawlTaskQueryPort,
            CrawlTaskTransactionManager crawlTaskTransactionManager,
            CrawlExecutionManager crawlExecutionManager,
            CrawlResultProcessorProvider processorProvider,
            CreateCrawlTaskUseCase createCrawlTaskUseCase) {
        this.crawlTaskQueryPort = crawlTaskQueryPort;
        this.crawlTaskTransactionManager = crawlTaskTransactionManager;
        this.crawlExecutionManager = crawlExecutionManager;
        this.processorProvider = processorProvider;
        this.createCrawlTaskUseCase = createCrawlTaskUseCase;
    }

    /**
     * CrawlTask 실행 준비 (RUNNING 상태로 전환)
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>CrawlTask 조회
     *   <li>CrawlTask 상태 → RUNNING
     *   <li>CrawlExecution 생성 (RUNNING 상태)
     * </ol>
     *
     * @param command 실행 커맨드
     * @return 실행 컨텍스트 (CrawlTask + CrawlExecution)
     * @throws RuntimeException CrawlTask가 존재하지 않는 경우
     */
    @Transactional
    public ExecutionContext prepareExecution(ExecuteCrawlTaskCommand command) {
        Long taskId = command.taskId();

        log.info("CrawlTask 실행 준비 시작: taskId={}, schedulerId={}", taskId, command.schedulerId());

        // 1. CrawlTask 조회
        CrawlTask crawlTask = findCrawlTaskOrThrow(taskId);

        // 2. CrawlTask 상태 → RUNNING
        crawlTask.markAsRunning();
        crawlTaskTransactionManager.persist(crawlTask);

        log.debug("CrawlTask 상태 업데이트: taskId={}, status=RUNNING", taskId);

        // 3. CrawlExecution 생성 및 저장 (RUNNING 상태)
        CrawlExecution execution =
                crawlExecutionManager.startAndPersist(
                        crawlTask.getId(),
                        CrawlSchedulerId.of(command.schedulerId()),
                        SellerId.of(command.sellerId()));

        log.info("CrawlTask 실행 준비 완료: taskId={}", taskId);

        return new ExecutionContext(crawlTask, execution);
    }

    /**
     * CrawlTask 실행 성공 처리
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>CrawlExecution 성공 완료 및 저장
     *   <li>CrawlTask 상태 → SUCCESS
     *   <li>크롤링 결과 처리 (파싱, 저장, 후속 Task 생성)
     * </ol>
     *
     * @param context 실행 컨텍스트
     * @param crawlResult 크롤링 결과
     */
    @Transactional
    public void completeWithSuccess(ExecutionContext context, CrawlResult crawlResult) {
        CrawlTask crawlTask = context.crawlTask();
        CrawlExecution execution = context.execution();

        log.debug("CrawlTask 성공 처리 시작: taskId={}", crawlTask.getId().value());

        // 1. CrawlExecution 성공 완료 및 저장
        crawlExecutionManager.completeWithSuccess(
                execution, crawlResult.getResponseBody(), crawlResult.getHttpStatusCode());

        // 2. CrawlTask 상태 → SUCCESS
        crawlTask.markAsSuccess();
        crawlTaskTransactionManager.persist(crawlTask);

        // 3. 크롤링 결과 처리 (파싱 + 저장 + 후속 Task 생성)
        processResult(crawlResult, crawlTask);

        log.info(
                "CrawlTask 실행 성공: taskId={}, durationMs={}",
                crawlTask.getId().value(),
                execution.getDuration().durationMs());
    }

    /**
     * 크롤링 결과 처리
     *
     * <p>CrawlResultProcessor를 통해 결과를 파싱하고, 비즈니스 데이터를 저장하고, 후속 Task를 생성합니다.
     *
     * @param crawlResult 크롤링 결과
     * @param crawlTask 처리 대상 CrawlTask
     */
    private void processResult(CrawlResult crawlResult, CrawlTask crawlTask) {
        CrawlResultProcessor processor = processorProvider.getProcessor(crawlTask.getTaskType());
        ProcessingResult processingResult = processor.process(crawlResult, crawlTask);

        log.debug(
                "크롤링 결과 처리 완료: taskType={}, parsedItems={}, savedItems={}, followUpTasks={}",
                crawlTask.getTaskType(),
                processingResult.getParsedItemCount(),
                processingResult.getSavedItemCount(),
                processingResult.getFollowUpCommands().size());

        // 후속 Task 생성
        if (processingResult.hasFollowUpTasks()) {
            createCrawlTaskUseCase.executeBatch(processingResult.getFollowUpCommands());
            log.info(
                    "후속 CrawlTask 생성 요청: taskType={}, count={}",
                    crawlTask.getTaskType(),
                    processingResult.getFollowUpCommands().size());
        }
    }

    /**
     * CrawlTask 실행 실패 처리
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>CrawlExecution 실패 완료 및 저장
     *   <li>CrawlTask 상태 → FAILED
     * </ol>
     *
     * @param context 실행 컨텍스트
     * @param httpStatusCode HTTP 상태 코드 (nullable)
     * @param errorMessage 에러 메시지
     */
    @Transactional
    public void completeWithFailure(
            ExecutionContext context, Integer httpStatusCode, String errorMessage) {
        CrawlTask crawlTask = context.crawlTask();
        CrawlExecution execution = context.execution();

        log.debug("CrawlTask 실패 처리 시작: taskId={}", crawlTask.getId().value());

        // 1. CrawlExecution 실패 완료 및 저장
        crawlExecutionManager.completeWithFailure(execution, httpStatusCode, errorMessage);

        // 2. CrawlTask 상태 → FAILED
        crawlTask.markAsFailed();
        crawlTaskTransactionManager.persist(crawlTask);

        log.warn(
                "CrawlTask 실행 실패: taskId={}, httpStatus={}, error={}, durationMs={}",
                crawlTask.getId().value(),
                httpStatusCode,
                errorMessage,
                execution.getDuration().durationMs());
    }

    /**
     * CrawlTask 실행 타임아웃 처리
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>CrawlExecution 타임아웃 완료 및 저장
     *   <li>CrawlTask 상태 → FAILED
     * </ol>
     *
     * @param context 실행 컨텍스트
     * @param errorMessage 타임아웃 에러 메시지
     */
    @Transactional
    public void completeWithTimeout(ExecutionContext context, String errorMessage) {
        CrawlTask crawlTask = context.crawlTask();
        CrawlExecution execution = context.execution();

        log.debug("CrawlTask 타임아웃 처리 시작: taskId={}", crawlTask.getId().value());

        // 1. CrawlExecution 타임아웃 완료 및 저장
        crawlExecutionManager.completeWithTimeout(execution, errorMessage);

        // 2. CrawlTask 상태 → FAILED
        crawlTask.markAsFailed();
        crawlTaskTransactionManager.persist(crawlTask);

        log.warn(
                "CrawlTask 실행 타임아웃: taskId={}, error={}, durationMs={}",
                crawlTask.getId().value(),
                errorMessage,
                execution.getDuration().durationMs());
    }

    /**
     * CrawlTask 조회 (없으면 예외)
     *
     * @param taskId CrawlTask ID
     * @return CrawlTask
     * @throws RuntimeException 태스크가 존재하지 않는 경우
     */
    private CrawlTask findCrawlTaskOrThrow(Long taskId) {
        CrawlTaskId crawlTaskId = CrawlTaskId.of(taskId);
        return crawlTaskQueryPort
                .findById(crawlTaskId)
                .orElseThrow(
                        () -> {
                            log.error("CrawlTask를 찾을 수 없습니다: taskId={}", taskId);
                            return new CrawlTaskNotFoundException(taskId);
                        });
    }
}
