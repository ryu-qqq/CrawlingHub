package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.dto.command.BatchRetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.BatchRetryResultResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.BatchRetryResultResponse.RetryFailureItem;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.BatchRetryCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 배치 재시도 Service
 *
 * <p>BatchRetryCrawlTaskUseCase 구현체
 *
 * <ul>
 *   <li>여러 실패한 CrawlTask를 일괄 재실행
 *   <li>개별 Task의 재시도 가능 여부 검증
 *   <li>성공/실패 결과를 분리하여 반환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class BatchRetryCrawlTaskService implements BatchRetryCrawlTaskUseCase {

    private final CrawlTaskReadManager readManager;
    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskFacade facade;
    private final TimeProvider timeProvider;

    public BatchRetryCrawlTaskService(
            CrawlTaskReadManager readManager,
            CrawlTaskCommandFactory commandFactory,
            CrawlTaskFacade facade,
            TimeProvider timeProvider) {
        this.readManager = readManager;
        this.commandFactory = commandFactory;
        this.facade = facade;
        this.timeProvider = timeProvider;
    }

    @Override
    public BatchRetryResultResponse retryBatch(BatchRetryCrawlTaskCommand command) {
        List<Long> successIds = new ArrayList<>();
        List<RetryFailureItem> failures = new ArrayList<>();

        for (Long taskId : command.crawlTaskIds()) {
            processRetry(taskId, successIds, failures);
        }

        return BatchRetryResultResponse.of(successIds, failures);
    }

    private void processRetry(Long taskId, List<Long> successIds, List<RetryFailureItem> failures) {
        CrawlTaskId crawlTaskId = CrawlTaskId.of(taskId);

        Optional<CrawlTask> taskOptional = readManager.findById(crawlTaskId);
        if (taskOptional.isEmpty()) {
            failures.add(new RetryFailureItem(taskId, "Task를 찾을 수 없습니다."));
            return;
        }

        CrawlTask crawlTask = taskOptional.get();

        boolean canRetry = crawlTask.attemptRetry(timeProvider.now());
        if (!canRetry) {
            String reason =
                    String.format(
                            "재시도 불가 - 상태: %s, 재시도 횟수: %d",
                            crawlTask.getStatus().name(), crawlTask.getRetryCount().value());
            failures.add(new RetryFailureItem(taskId, reason));
            return;
        }

        try {
            String outboxPayload = commandFactory.toOutboxPayload(crawlTask);
            facade.retry(crawlTask, outboxPayload);
            successIds.add(taskId);
        } catch (Exception e) {
            failures.add(new RetryFailureItem(taskId, "재시도 처리 중 오류 발생: " + e.getMessage()));
        }
    }
}
