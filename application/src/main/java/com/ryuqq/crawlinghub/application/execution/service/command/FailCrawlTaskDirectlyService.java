package com.ryuqq.crawlinghub.application.execution.service.command;

import com.ryuqq.crawlinghub.application.common.dto.command.StatusChangeContext;
import com.ryuqq.crawlinghub.application.common.metric.annotation.CrawlMetric;
import com.ryuqq.crawlinghub.application.execution.port.in.command.FailCrawlTaskDirectlyUseCase;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 즉시 실패 처리 Service
 *
 * <p><strong>용도</strong>: RUNNING 전환 전에 발생한 영구적 오류에 대해 CrawlTask를 즉시 FAILED 상태로 전환
 *
 * <p><strong>멱등성</strong>: 이미 종료 상태(SUCCESS, FAILED)인 Task는 스킵합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class FailCrawlTaskDirectlyService implements FailCrawlTaskDirectlyUseCase {

    private static final Logger log = LoggerFactory.getLogger(FailCrawlTaskDirectlyService.class);

    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskReadManager crawlTaskReadManager;
    private final CrawlTaskCommandManager crawlTaskCommandManager;

    public FailCrawlTaskDirectlyService(
            CrawlTaskCommandFactory commandFactory,
            CrawlTaskReadManager crawlTaskReadManager,
            CrawlTaskCommandManager crawlTaskCommandManager) {
        this.commandFactory = commandFactory;
        this.crawlTaskReadManager = crawlTaskReadManager;
        this.crawlTaskCommandManager = crawlTaskCommandManager;
    }

    @CrawlMetric(value = "crawl_task", operation = "fail_directly")
    @Override
    public void execute(Long taskId, String reason) {
        // 1. Factory → StatusChangeContext 생성 (ID 변환 + 시간 주입)
        StatusChangeContext<CrawlTaskId> context = commandFactory.createStatusChangeContext(taskId);

        // 2. Task 조회 → 종료 상태 스킵 → 실패 처리 + 저장
        crawlTaskReadManager
                .findById(context.id())
                .ifPresentOrElse(
                        task -> failIfNotTerminal(task, context, reason),
                        () ->
                                log.warn(
                                        "CrawlTask 존재하지 않음 (스킵): taskId={}, reason={}",
                                        taskId,
                                        reason));
    }

    private void failIfNotTerminal(
            CrawlTask task, StatusChangeContext<CrawlTaskId> context, String reason) {
        if (task.isTerminal()) {
            log.info(
                    "CrawlTask 이미 종료 상태 (스킵): taskId={}, status={}",
                    context.id(),
                    task.getStatus());
            return;
        }

        task.failDirectly(context.changedAt());
        crawlTaskCommandManager.persist(task);

        log.warn(
                "CrawlTask 즉시 실패 처리: taskId={}, previousStatus={}, reason={}",
                context.id(),
                task.getStatus(),
                reason);
    }
}
