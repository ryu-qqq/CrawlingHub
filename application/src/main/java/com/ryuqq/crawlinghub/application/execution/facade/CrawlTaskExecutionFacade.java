package com.ryuqq.crawlinghub.application.execution.facade;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.processor.CrawlResultProcessor;
import com.ryuqq.crawlinghub.application.crawl.processor.CrawlResultProcessorProvider;
import com.ryuqq.crawlinghub.application.crawl.processor.ProcessingResult;
import com.ryuqq.crawlinghub.application.execution.dto.ExecutionContext;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.manager.CrawlExecutionTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.command.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.CreateCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    private final CrawlTaskReadManager crawlTaskReadManager;
    private final CrawlTaskTransactionManager crawlTaskTransactionManager;
    private final CrawlExecutionTransactionManager crawlExecutionManager;
    private final CrawlResultProcessorProvider processorProvider;
    private final CreateCrawlTaskUseCase createCrawlTaskUseCase;
    private final ClockHolder clockHolder;

    public CrawlTaskExecutionFacade(
            CrawlTaskReadManager crawlTaskReadManager,
            CrawlTaskTransactionManager crawlTaskTransactionManager,
            CrawlExecutionTransactionManager crawlExecutionManager,
            CrawlResultProcessorProvider processorProvider,
            CreateCrawlTaskUseCase createCrawlTaskUseCase,
            ClockHolder clockHolder) {
        this.crawlTaskReadManager = crawlTaskReadManager;
        this.crawlTaskTransactionManager = crawlTaskTransactionManager;
        this.crawlExecutionManager = crawlExecutionManager;
        this.processorProvider = processorProvider;
        this.createCrawlTaskUseCase = createCrawlTaskUseCase;
        this.clockHolder = clockHolder;
    }

    /**
     * CrawlTask 실행 준비 (RUNNING 상태로 전환)
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>CrawlTask 조회
     *   <li>멱등성 체크 - 이미 처리 완료된 Task인지 확인
     *   <li>CrawlTask 상태 → RUNNING
     *   <li>CrawlExecution 생성 (RUNNING 상태)
     * </ol>
     *
     * <p><strong>멱등성 보장</strong>: SQS 중복 메시지로 인해 이미 완료된 Task에 대해 재처리 요청이 올 경우, 예외를 던지지 않고
     * Optional.empty()를 반환하여 호출자가 안전하게 처리를 스킵할 수 있도록 합니다.
     *
     * @param command 실행 커맨드
     * @return 실행 컨텍스트 (CrawlTask + CrawlExecution), 이미 처리 완료된 경우 Optional.empty()
     * @throws CrawlTaskNotFoundException CrawlTask가 존재하지 않는 경우
     */
    @Transactional
    public Optional<ExecutionContext> prepareExecution(ExecuteCrawlTaskCommand command) {
        Long taskId = command.taskId();

        log.info("CrawlTask 실행 준비 시작: taskId={}, schedulerId={}", taskId, command.schedulerId());

        // 1. CrawlTask 조회
        CrawlTask crawlTask = findCrawlTaskOrThrow(taskId);

        // 2. 멱등성 체크 - 이미 완료된 Task인지 확인
        CrawlTaskStatus currentStatus = crawlTask.getStatus();
        if (currentStatus.isTerminal()) {
            log.info(
                    "CrawlTask 이미 처리 완료 (멱등성 스킵): taskId={}, currentStatus={}",
                    taskId,
                    currentStatus);
            return Optional.empty();
        }

        // 3. PUBLISHED 상태가 아닌 경우 (RUNNING 등) 처리 스킵
        if (currentStatus != CrawlTaskStatus.PUBLISHED) {
            log.warn(
                    "CrawlTask 처리 불가 상태 (스킵): taskId={}, currentStatus={},"
                            + " expectedStatus=PUBLISHED",
                    taskId,
                    currentStatus);
            return Optional.empty();
        }

        // 4. CrawlTask 상태 → RUNNING
        crawlTask.markAsRunning(clockHolder.getClock());
        crawlTaskTransactionManager.persist(crawlTask);

        log.debug("CrawlTask 상태 업데이트: taskId={}, status=RUNNING", taskId);

        // 5. CrawlExecution 생성 및 저장 (RUNNING 상태)
        CrawlExecution execution =
                crawlExecutionManager.startAndPersist(
                        crawlTask.getId(),
                        CrawlSchedulerId.of(command.schedulerId()),
                        SellerId.of(command.sellerId()));

        log.info("CrawlTask 실행 준비 완료: taskId={}", taskId);

        return Optional.of(new ExecutionContext(crawlTask, execution));
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

        log.info(
                "🔵 [TX-START] completeWithSuccess 트랜잭션 시작: taskId={}, txActive={}, txName={}",
                crawlTask.getId().value(),
                TransactionSynchronizationManager.isActualTransactionActive(),
                TransactionSynchronizationManager.getCurrentTransactionName());

        // 1. CrawlExecution 성공 완료 및 저장
        crawlExecutionManager.completeWithSuccess(
                execution, crawlResult.getResponseBody(), crawlResult.getHttpStatusCode());
        log.debug("✅ Step 1 완료: CrawlExecution 성공 처리");

        // 2. CrawlTask 상태 → SUCCESS
        crawlTask.markAsSuccess(clockHolder.getClock());
        crawlTaskTransactionManager.persist(crawlTask);
        log.debug("✅ Step 2 완료: CrawlTask SUCCESS 마킹");

        // 3. 크롤링 결과 처리 (파싱 + 저장 + 후속 Task 생성)
        log.debug("🔄 Step 3 시작: processResult 호출");
        processResult(crawlResult, crawlTask);
        log.debug("✅ Step 3 완료: processResult 처리 완료");

        log.info(
                "🟢 [TX-END] completeWithSuccess 트랜잭션 종료 예정: taskId={}, durationMs={}, "
                        + "txActive={} (이 로그 후 커밋 시도)",
                crawlTask.getId().value(),
                execution.getDuration().durationMs(),
                TransactionSynchronizationManager.isActualTransactionActive());
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
        crawlTask.markAsFailed(clockHolder.getClock());
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
        crawlTask.markAsFailed(clockHolder.getClock());
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
        return crawlTaskReadManager
                .findById(crawlTaskId)
                .orElseThrow(
                        () -> {
                            log.error("CrawlTask를 찾을 수 없습니다: taskId={}", taskId);
                            return new CrawlTaskNotFoundException(taskId);
                        });
    }
}
