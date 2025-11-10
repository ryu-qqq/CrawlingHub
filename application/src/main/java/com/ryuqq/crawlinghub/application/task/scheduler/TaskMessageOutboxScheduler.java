package com.ryuqq.crawlinghub.application.task.scheduler;

import com.ryuqq.crawlinghub.application.task.manager.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.application.task.port.out.PublishTaskMessagePort;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Task 메시지 Outbox 스케줄러
 *
 * <p>PENDING 상태의 메시지를 주기적으로 재시도
 *
 * <p>실행 주기: 1분마다
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class TaskMessageOutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskMessageOutboxScheduler.class);

    private final TaskMessageOutboxManager taskMessageOutboxManager;
    private final PublishTaskMessagePort publishTaskMessagePort;

    public TaskMessageOutboxScheduler(
        TaskMessageOutboxManager taskMessageOutboxManager,
        PublishTaskMessagePort publishTaskMessagePort
    ) {
        this.taskMessageOutboxManager = taskMessageOutboxManager;
        this.publishTaskMessagePort = publishTaskMessagePort;
    }

    /**
     * PENDING 상태의 메시지 재발행 (1분마다)
     */
    @Scheduled(fixedDelay = 60_000) // 1분
    public void retryPendingMessages() {
        try {
            log.info("PENDING 메시지 재발행 시작");

            // 1. PENDING 메시지 조회
            List<TaskMessageOutbox> pendingMessages = taskMessageOutboxManager.findPendingMessages();

            if (pendingMessages.isEmpty()) {
                log.debug("재발행할 PENDING 메시지 없음");
                return;
            }

            log.info("재발행 대상 메시지 수: {}", pendingMessages.size());

            // 2. 재시도 가능한 메시지만 발행
            int successCount = 0;
            int failureCount = 0;

            for (TaskMessageOutbox outbox : pendingMessages) {
                if (!outbox.canRetry()) {
                    log.warn("최대 재시도 횟수 초과. outboxId={}, retryCount={}",
                        outbox.getOutboxId(), outbox.getRetryCount());
                    continue;
                }

                try {
                    // 3. SQS 발행
                    publishTaskMessagePort.publish(outbox.getTaskId(), outbox.getTaskType());

                    // 4. 발행 성공 처리
                    taskMessageOutboxManager.markSent(outbox.getOutboxId());
                    successCount++;

                    log.info("재발행 성공. outboxId={}, taskId={}, taskType={}",
                        outbox.getOutboxId(), outbox.getTaskId(), outbox.getTaskType());

                } catch (Exception e) {
                    // 5. 발행 실패 처리
                    taskMessageOutboxManager.markFailed(outbox.getOutboxId(), e.getMessage());
                    failureCount++;

                    log.error("재발행 실패. outboxId={}, retryCount={}, error={}",
                        outbox.getOutboxId(), outbox.getRetryCount(), e.getMessage(), e);
                }
            }

            log.info("PENDING 메시지 재발행 완료. 성공: {}, 실패: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("PENDING 메시지 재발행 중 오류 발생", e);
        }
    }
}
