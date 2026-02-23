package com.ryuqq.crawlinghub.application.execution.service.command;

import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.factory.command.CrawlExecutionCommandFactory;
import com.ryuqq.crawlinghub.application.execution.internal.CrawlTaskExecutionCoordinator;
import com.ryuqq.crawlinghub.application.execution.port.in.command.CrawlTaskExecutionUseCase;
import com.ryuqq.crawlinghub.application.execution.validator.CrawlTaskExecutionValidator;
import com.ryuqq.crawlinghub.domain.execution.exception.RetryableExecutionException;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

/**
 * CrawlTask 실행 Service (Thin Orchestrator)
 *
 * <p><strong>용도</strong>: SQS에서 수신한 CrawlTask 메시지를 처리하여 크롤링 실행
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>validateOrSkip: CrawlTask 조회 → 멱등성 체크 → 상태 검증
 *   <li>Bundle 생성: Factory → CrawlTaskExecutionBundle (execution + changedAt 포함)
 *   <li>Coordinator.execute: 전체 크롤링 흐름 조율 (prepare → crawl → complete)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class CrawlTaskExecutionService implements CrawlTaskExecutionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskExecutionService.class);

    private final CrawlTaskExecutionValidator validator;
    private final CrawlExecutionCommandFactory commandFactory;
    private final CrawlTaskExecutionCoordinator coordinator;

    public CrawlTaskExecutionService(
            CrawlTaskExecutionValidator validator,
            CrawlExecutionCommandFactory commandFactory,
            CrawlTaskExecutionCoordinator coordinator) {
        this.validator = validator;
        this.commandFactory = commandFactory;
        this.coordinator = coordinator;
    }

    @Override
    public void execute(ExecuteCrawlTaskCommand command) {
        Long taskId = command.taskId();

        log.info(
                "CrawlTask 실행 시작: taskId={}, schedulerId={}, taskType={}",
                taskId,
                command.schedulerId(),
                command.taskType());

        Optional<CrawlTask> taskOptional = validateOrSkip(taskId, command);
        if (taskOptional.isEmpty()) {
            return;
        }

        CrawlTaskExecutionBundle bundle =
                commandFactory.createExecutionBundle(taskOptional.get(), command);

        coordinator.execute(bundle);
    }

    /**
     * CrawlTask 검증 및 스킵 판단
     *
     * <p>인프라 오류(DB 커넥션 실패 등) 시 RetryableExecutionException을 던져 SQS 재시도를 유도합니다. 이미 처리 완료된 Task이거나 처리
     * 불가 상태인 경우 Optional.empty()를 반환합니다.
     *
     * @param taskId CrawlTask ID
     * @param command 실행 커맨드 (로깅용)
     * @return 실행 가능한 CrawlTask, 스킵 시 Optional.empty()
     * @throws RetryableExecutionException 인프라 오류 시
     */
    private Optional<CrawlTask> validateOrSkip(Long taskId, ExecuteCrawlTaskCommand command) {
        Optional<CrawlTask> taskOptional;
        try {
            taskOptional = validator.validateAndGet(taskId);
        } catch (DataAccessException | TransactionException e) {
            throw new RetryableExecutionException("prepareExecution 인프라 오류: taskId=" + taskId, e);
        }

        if (taskOptional.isEmpty()) {
            log.info(
                    "CrawlTask 이미 처리됨 또는 처리 불가 (정상 종료): taskId={}, schedulerId={}",
                    taskId,
                    command.schedulerId());
        }

        return taskOptional;
    }
}
