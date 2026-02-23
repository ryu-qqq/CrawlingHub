package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskCommandFacade;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.RetryCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskRetryException;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 재시도 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RetryCrawlTaskService implements RetryCrawlTaskUseCase {

    private final CrawlTaskReadManager readManager;
    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskAssembler assembler;
    private final CrawlTaskCommandFacade coordinator;
    private final TimeProvider timeProvider;

    public RetryCrawlTaskService(
            CrawlTaskReadManager readManager,
            CrawlTaskCommandFactory commandFactory,
            CrawlTaskAssembler assembler,
            CrawlTaskCommandFacade coordinator,
            TimeProvider timeProvider) {
        this.readManager = readManager;
        this.commandFactory = commandFactory;
        this.assembler = assembler;
        this.coordinator = coordinator;
        this.timeProvider = timeProvider;
    }

    @Override
    public CrawlTaskResult retry(RetryCrawlTaskCommand command) {
        CrawlTaskId crawlTaskId = CrawlTaskId.of(command.crawlTaskId());

        // 1. CrawlTask 조회
        CrawlTask crawlTask =
                readManager
                        .findById(crawlTaskId)
                        .orElseThrow(() -> new CrawlTaskNotFoundException(command.crawlTaskId()));

        // 2. 재시도 시도 (도메인 로직: 상태/횟수 검증)
        boolean success = crawlTask.attemptRetry(timeProvider.now());
        if (!success) {
            throw new CrawlTaskRetryException(
                    command.crawlTaskId(), crawlTask.getStatus(), crawlTask.getRetryCountValue());
        }

        // 3. 재시도용 번들 생성
        CrawlTaskBundle retryBundle = commandFactory.createRetryBundle(crawlTask);

        // 4. Coordinator에서 업데이트 + Outbox 저장
        coordinator.retry(crawlTask, retryBundle);

        // 5. 응답 변환
        return assembler.toResult(crawlTask);
    }
}
