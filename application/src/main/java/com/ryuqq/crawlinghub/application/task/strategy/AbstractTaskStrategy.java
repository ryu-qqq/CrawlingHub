package com.ryuqq.crawlinghub.application.task.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.crawl.result.component.CrawlResultManager;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlResult;
import com.ryuqq.crawlinghub.application.task.facade.CrawlerFacade;
import com.ryuqq.crawlinghub.application.task.component.TaskManager;
import com.ryuqq.crawlinghub.application.task.component.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task 처리 전략 추상 클래스 (Template Method Pattern + Generic)
 *
 * <p>공통 처리 흐름:
 * <ol>
 *   <li>CrawlerFacade를 통한 크롤링 실행</li>
 *   <li>executeTask() 실행 (하위 클래스가 구현, 제네릭 타입 지원)</li>
 *   <li>Task 완료 처리 및 저장</li>
 *   <li>후속 Task 생성 및 Outbox 저장</li>
 *   <li>Transaction 커밋 → TransactionalEventListener → SQS 발행</li>
 * </ol>
 *
 * <p>제네릭 타입 파라미터:
 * - OUTPUT: 크롤링 결과 타입 (MiniShopOutput, ProductDetailOutput 등)
 *
 * @param <OUTPUT> 크롤링 응답 DTO 타입
 * @author ryu-qqq
 * @since 2025-11-06
 */
public abstract class AbstractTaskStrategy<OUTPUT> implements TaskStrategy {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final TaskManager taskManager;
    protected final TaskMessageOutboxManager taskMessageOutboxManager;
    protected final CrawlerFacade crawlerFacade;
    protected final CrawlResultManager crawlResultManager;
    protected final ObjectMapper objectMapper;

    protected AbstractTaskStrategy(
        TaskManager taskManager,
        TaskMessageOutboxManager taskMessageOutboxManager,
        CrawlerFacade crawlerFacade,
        CrawlResultManager crawlResultManager,
        ObjectMapper objectMapper
    ) {
        this.taskManager = taskManager;
        this.taskMessageOutboxManager = taskMessageOutboxManager;
        this.crawlerFacade = crawlerFacade;
        this.crawlResultManager = crawlResultManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public final void execute(Task task) {
        try {
            log.info("Task 실행 시작. taskId={}, taskType={}", task.getIdValue(), task.getTaskType());

            // 1. 크롤링 실행 (CrawlerFacade)
            CrawlResult<OUTPUT> result = crawlerFacade.execute(task, getOutputType());

            // 2. 크롤링 성공 시 JSON 저장 + 결과 처리
            if (result.isSuccess()) {
                // 2-1. 크롤링 결과를 JSON으로 저장
                saveCrawlResultAsJson(task, result.getData());

                // 2-2. 결과 처리 (하위 클래스가 구현)
                executeTask(task, result.getData());
            } else {
                handleFailure(task, result.getError(), result.getStatusCode());
                return;
            }

            // 3. Task 완료 처리
            task.completeSuccessfully();
            taskManager.saveTask(task);

            log.info("Task 실행 완료. taskId={}, taskType={}", task.getIdValue(), task.getTaskType());

        } catch (Exception e) {
            log.error("Task 실행 실패. taskId={}, taskType={}, error={}",
                task.getIdValue(), task.getTaskType(), e.getMessage(), e);

            // Task 실패 처리
            handleFailure(task, e.getMessage(), null);

            throw e;
        }
    }

    /**
     * 실제 Task 처리 로직 (하위 클래스가 구현)
     *
     * <p>크롤링 결과를 받아서 처리:
     * - META: 셀러 상품 수 업데이트 + MINI_SHOP Task 생성
     * - MINI_SHOP: 상품 목록 저장 + PRODUCT_DETAIL/OPTION Task 생성
     * - PRODUCT_DETAIL: 상품 상세 정보 저장
     * - PRODUCT_OPTION: 상품 옵션 정보 저장
     *
     * @param task   처리할 Task
     * @param output 크롤링 결과 데이터
     */
    protected abstract void executeTask(Task task, OUTPUT output);

    /**
     * Output 타입 반환 (제네릭 타입 정보)
     *
     * @return Output 클래스 타입
     */
    protected abstract Class<OUTPUT> getOutputType();

    /**
     * 크롤링 결과를 JSON으로 저장
     *
     * <p>크롤링 API 응답을 JSON 문자열로 변환하여 DB에 저장합니다.
     * <p>JSON 변환 실패 시 경고 로그만 기록하고 Task 실행은 계속됩니다.
     *
     * @param task   처리 중인 Task
     * @param output 크롤링 결과 데이터
     */
    private void saveCrawlResultAsJson(Task task, OUTPUT output) {
        try {
            // 1. OUTPUT → JSON 변환
            String rawData = objectMapper.writeValueAsString(output);

            // 2. CrawlResult 저장
            Long crawlResultId = crawlResultManager.saveCrawlResult(
                TaskId.of(task.getIdValue()),
                task.getTaskType(),
                MustItSellerId.of(task.getSellerIdValue()),
                rawData
            );

            log.debug("크롤링 결과 JSON 저장 완료. taskId={}, crawlResultId={}, dataSize={}",
                task.getIdValue(), crawlResultId, rawData.length());

        } catch (JsonProcessingException e) {
            log.warn("크롤링 결과 JSON 변환 실패. taskId={}, taskType={}, error={}",
                task.getIdValue(), task.getTaskType(), e.getMessage());
        }
    }

    /**
     * 실패 처리
     */
    private void handleFailure(Task task, String error, Integer statusCode) {
        task.failWithError(error);
        taskManager.saveTask(task);

        log.error("Task 실패 처리 완료. taskId={}, error={}, statusCode={}",
            task.getIdValue(), error, statusCode);
    }

    /**
     * 후속 Task 생성 및 Outbox 저장 헬퍼 메서드
     *
     * <p>동적 Task 생성:
     * - META/MINI_SHOP: 크롤링 결과에 따라 동적으로 Task 생성
     * - TaskMessage Outbox에 저장 후 이벤트 발행
     * - Transaction 커밋 후 자동으로 SQS 발행
     *
     * @param tasks 생성할 Task 배열
     */
    protected void createAndPublishTasks(Task... tasks) {
        for (Task task : tasks) {
            // 1. Task 저장
            Task savedTask = taskManager.saveTask(task);

            // 2. Outbox 저장 (PENDING) + Event 발행
            taskMessageOutboxManager.createTaskMessage(
                TaskId.of(savedTask.getIdValue()),
                savedTask.getTaskType()
            );
        }
    }
}
