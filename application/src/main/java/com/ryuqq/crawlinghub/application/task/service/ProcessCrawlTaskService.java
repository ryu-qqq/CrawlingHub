package com.ryuqq.crawlinghub.application.task.service;


import com.ryuqq.crawlinghub.application.task.port.in.ProcessMiniShopResultUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.ProcessProductDetailUseCase;
import com.ryuqq.crawlinghub.application.task.assembler.command.MiniShopResultCommand;
import com.ryuqq.crawlinghub.application.task.assembler.command.ProductDetailCommand;
import com.ryuqq.crawlinghub.application.task.port.out.LoadCrawlTaskPort;
import com.ryuqq.crawlinghub.application.task.port.out.SaveCrawlTaskPort;
import com.ryuqq.crawlinghub.application.task.command.ProcessTaskCommand;
import com.ryuqq.crawlinghub.application.task.command.TaskFailureCommand;
import com.ryuqq.crawlinghub.application.task.port.in.HandleTaskFailureUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.ProcessCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.HttpCrawlerPort;
import com.ryuqq.crawlinghub.application.task.port.out.TokenManagerPort;
import com.ryuqq.crawlinghub.application.task.port.out.UserAgentPort;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 크롤링 태스크 처리 UseCase 구현체 (SQS Consumer)
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>외부 API 호출이 메인 로직이므로 트랜잭션 사용 안 함</li>
 *   <li>상태 업데이트만 별도 트랜잭션으로 실행</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class ProcessCrawlTaskService implements ProcessCrawlTaskUseCase {

    private final LoadCrawlTaskPort loadCrawlTaskPort;
    private final SaveCrawlTaskPort saveCrawlTaskPort;
    private final UserAgentPort userAgentPort;
    private final TokenManagerPort tokenManagerPort;
    private final HttpCrawlerPort httpCrawlerPort;
    private final ProcessMiniShopResultUseCase processMiniShopResultUseCase;
    private final ProcessProductDetailUseCase processProductDetailUseCase;
    private final HandleTaskFailureUseCase handleTaskFailureUseCase;

    public ProcessCrawlTaskService(
        LoadCrawlTaskPort loadCrawlTaskPort,
        SaveCrawlTaskPort saveCrawlTaskPort,
        UserAgentPort userAgentPort,
        TokenManagerPort tokenManagerPort,
        HttpCrawlerPort httpCrawlerPort,
        ProcessMiniShopResultUseCase processMiniShopResultUseCase,
        ProcessProductDetailUseCase processProductDetailUseCase,
        HandleTaskFailureUseCase handleTaskFailureUseCase
    ) {
        this.loadCrawlTaskPort = loadCrawlTaskPort;
        this.saveCrawlTaskPort = saveCrawlTaskPort;
        this.userAgentPort = userAgentPort;
        this.tokenManagerPort = tokenManagerPort;
        this.httpCrawlerPort = httpCrawlerPort;
        this.processMiniShopResultUseCase = processMiniShopResultUseCase;
        this.processProductDetailUseCase = processProductDetailUseCase;
        this.handleTaskFailureUseCase = handleTaskFailureUseCase;
    }

    /**
     * 태스크 처리 (크롤링 실행)
     *
     * <p>⚠️ 트랜잭션 없음 - 외부 API 호출이 메인 로직
     *
     * <p>실행 순서:
     * 1. 태스크 조회 및 시작 처리 (트랜잭션)
     * 2. User-Agent 선택 (트랜잭션 밖)
     * 3. 토큰 확인 (트랜잭션 밖)
     * 4. HTTP API 호출 (트랜잭션 밖)
     * 5. 결과 처리 분기 (성공/실패)
     *
     * @param command SQS 메시지
     */
    @Override
    public void execute(ProcessTaskCommand command) {
        CrawlTaskId taskId = CrawlTaskId.of(command.taskId());

        // 1. 태스크 조회 및 시작 처리 (트랜잭션)
        CrawlTask task = startTask(taskId);

        try {
            // 2. User-Agent 선택
            String userAgent = userAgentPort.selectUserAgent();

            // 3. 토큰 확인
            String token = tokenManagerPort.getToken(task.getSellerIdValue());
            if (token != null && tokenManagerPort.isExpired(token)) {
                throw new IllegalStateException("토큰이 만료되었습니다: " + task.getSellerIdValue());
            }

            // 4. HTTP API 호출 (외부)
            HttpCrawlerPort.CrawlResponse response = httpCrawlerPort.execute(
                task.getRequestUrlValue(),
                userAgent,
                token
            );

            // 5. 결과 처리 분기
            if (response.isSuccess()) {
                handleSuccess(task, response.body());
            } else {
                handleFailure(task, response.error(), response.statusCode());
            }

        } catch (Exception e) {
            // 예외 발생 시 실패 처리
            handleFailure(task, e.getMessage(), null);
        }
    }

    /**
     * 태스크 시작 처리 (트랜잭션)
     */
    @Transactional
    protected CrawlTask startTask(CrawlTaskId taskId) {
        CrawlTask task = loadCrawlTaskPort.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(
                "태스크를 찾을 수 없습니다: " + taskId.value()
            ));

        task.startProcessing();
        return saveCrawlTaskPort.save(task);
    }

    /**
     * 성공 처리
     */
    private void handleSuccess(CrawlTask task, String responseBody) {
        TaskType taskType = task.getTaskType();

        switch (taskType) {
            case MINI_SHOP -> {
                MiniShopResultCommand command = new MiniShopResultCommand(
                    task.getIdValue(),
                    responseBody
                );
                processMiniShopResultUseCase.execute(command);
            }
            case PRODUCT_DETAIL -> {
                // itemNo 추출 필요 (URL에서 또는 SQS 메시지에서)
                String itemNo = extractItemNo(task.getRequestUrlValue());
                ProductDetailCommand command = new ProductDetailCommand(
                    task.getIdValue(),
                    itemNo,
                    responseBody
                );
                processProductDetailUseCase.execute(command);
            }
            case PRODUCT_OPTION -> {
                // TODO: TASK-05에서 구현 예정
            }
        }
    }

    /**
     * 실패 처리
     */
    private void handleFailure(CrawlTask task, String error, Integer statusCode) {
        TaskFailureCommand command = new TaskFailureCommand(
            task.getIdValue(),
            error,
            statusCode
        );
        handleTaskFailureUseCase.execute(command);
    }

    /**
     * URL에서 itemNo 추출
     */
    private String extractItemNo(String url) {
        // URL 예시: https://api.smartstore.naver.com/products/12345/detail
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("products".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        throw new IllegalArgumentException("URL에서 itemNo를 추출할 수 없습니다: " + url);
    }
}
