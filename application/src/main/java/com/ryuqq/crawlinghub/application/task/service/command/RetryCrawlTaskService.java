package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.RetryCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskRetryException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 재시도 Service
 *
 * <p>RetryCrawlTaskUseCase 구현체
 *
 * <ul>
 *   <li>실패한 CrawlTask를 재실행
 *   <li>재시도 가능 여부 검증 (상태, 횟수)
 *   <li>Facade에 위임하여 트랜잭션 처리
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>ReadManager로 CrawlTask 조회
 *   <li>도메인에서 attemptRetry() 호출 (상태/횟수 검증 포함)
 *   <li>실패 시 CrawlTaskRetryException 발생
 *   <li>Facade에서 업데이트 + Outbox 생성 (트랜잭션)
 *   <li>Assembler로 응답 변환
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RetryCrawlTaskService implements RetryCrawlTaskUseCase {

    private final CrawlTaskReadManager readManager;
    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskAssembler assembler;
    private final CrawlTaskFacade facade;
    private final ClockHolder clockHolder;

    public RetryCrawlTaskService(
            CrawlTaskReadManager readManager,
            CrawlTaskCommandFactory commandFactory,
            CrawlTaskAssembler assembler,
            CrawlTaskFacade facade,
            ClockHolder clockHolder) {
        this.readManager = readManager;
        this.commandFactory = commandFactory;
        this.assembler = assembler;
        this.facade = facade;
        this.clockHolder = clockHolder;
    }

    @Override
    public CrawlTaskResponse retry(RetryCrawlTaskCommand command) {
        CrawlTaskId crawlTaskId = CrawlTaskId.of(command.crawlTaskId());

        // 1. CrawlTask 조회
        CrawlTask crawlTask =
                readManager
                        .findById(crawlTaskId)
                        .orElseThrow(() -> new CrawlTaskNotFoundException(command.crawlTaskId()));

        // 2. 재시도 시도 (도메인 로직: 상태/횟수 검증)
        boolean success = crawlTask.attemptRetry(clockHolder.getClock());
        if (!success) {
            throw new CrawlTaskRetryException(
                    command.crawlTaskId(),
                    crawlTask.getStatus(),
                    crawlTask.getRetryCount().value());
        }

        // 3. Outbox 페이로드 생성 (SQS 재발행용)
        String outboxPayload = commandFactory.toOutboxPayload(crawlTask);

        // 4. Facade에서 업데이트 + Outbox 저장 (트랜잭션)
        CrawlTask retriedTask = facade.retry(crawlTask, outboxPayload);

        // 5. 응답 변환
        return assembler.toResponse(retriedTask);
    }
}
