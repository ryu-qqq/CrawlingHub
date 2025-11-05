package com.ryuqq.crawlinghub.application.crawl.processing.service;


import com.ryuqq.crawlinghub.application.crawl.orchestration.port.out.LoadCrawlTaskPort;
import com.ryuqq.crawlinghub.application.crawl.orchestration.port.out.OutboxPort;
import com.ryuqq.crawlinghub.application.crawl.orchestration.port.out.SaveCrawlTaskPort;
import com.ryuqq.crawlinghub.application.crawl.processing.dto.command.RetryTasksCommand;
import com.ryuqq.crawlinghub.application.crawl.processing.port.in.RetryFailedTasksUseCase;
import com.ryuqq.crawlinghub.domain.crawl.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.crawl.task.TaskStatus;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 실패 태스크 재시도 UseCase 구현체
 *
 * <p>RETRY 또는 FAILED 상태의 태스크를 일괄 재시도합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class RetryFailedTasksService implements RetryFailedTasksUseCase {

    private final LoadCrawlTaskPort loadCrawlTaskPort;
    private final SaveCrawlTaskPort saveCrawlTaskPort;
    private final OutboxPort outboxPort;

    public RetryFailedTasksService(
        LoadCrawlTaskPort loadCrawlTaskPort,
        SaveCrawlTaskPort saveCrawlTaskPort,
        OutboxPort outboxPort
    ) {
        this.loadCrawlTaskPort = loadCrawlTaskPort;
        this.saveCrawlTaskPort = saveCrawlTaskPort;
        this.outboxPort = outboxPort;
    }

    /**
     * 실패 태스크 재시도
     *
     * <p>실행 순서:
     * 1. 실패 태스크 조회 (RETRY 또는 FAILED 상태)
     * 2. 재시도 조건 검증 (재시도 횟수 확인)
     * 3. 상태 초기화 (WAITING 또는 PUBLISHED)
     * 4. Outbox 재발행
     *
     * @param command 재시도 조건
     */
    @Override
    @Transactional
    public void execute(RetryTasksCommand command) {
        MustitSellerId sellerId = MustitSellerId.of(command.sellerId());

        // 1. 실패 태스크 조회
        // TODO: LoadCrawlTaskPort에 findFailedTasksBySellerId 메서드 추가 필요
        // 현재는 대기 중인 태스크만 조회 가능
        List<CrawlTask> waitingTasks = loadCrawlTaskPort.findWaitingTasksBySellerId(sellerId);

        // 필터링: RETRY 또는 FAILED 상태만
        List<CrawlTask> failedTasks = waitingTasks.stream()
            .filter(task -> task.hasStatus(TaskStatus.RETRY) || task.hasStatus(TaskStatus.FAILED))
            .filter(task -> command.taskType() == null || task.getTaskType() == command.taskType())
            .toList();

        // 2. 재시도 조건 검증 및 재발행
        failedTasks.forEach(task -> {
            // FAILED 상태는 재시도 횟수 초과이므로 건너뜀
            if (task.hasStatus(TaskStatus.FAILED) && !task.canRetry()) {
                return;
            }

            // 3. 상태 초기화
            // TODO: Domain에 resetForRetry() 메서드 추가 필요
            // task.resetForRetry();

            // 임시: 직접 발행 상태로 변경
            task.publish();
            saveCrawlTaskPort.save(task);

            // 4. Outbox 재발행
            republishTask(task);
        });
    }

    /**
     * 태스크 재발행
     */
    private void republishTask(CrawlTask task) {
        String payload = String.format(
            "{\"taskId\":%d,\"sellerId\":%d,\"taskType\":\"%s\",\"url\":\"%s\",\"retryCount\":%d}",
            task.getIdValue(),
            task.getSellerIdValue(),
            task.getTaskType().name(),
            task.getRequestUrlValue(),
            task.getRetryCount()
        );

        outboxPort.saveOutboxMessage(
            "CrawlTask",
            task.getIdValue(),
            "CrawlTaskRetry",
            payload
        );
    }
}
