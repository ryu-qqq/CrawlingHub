package com.ryuqq.crawlinghub.adapter.in.event.task;

import com.ryuqq.crawlinghub.domain.task.event.TaskMessageCreatedEvent;
import com.ryuqq.crawlinghub.application.task.manager.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.application.task.port.out.LoadTaskMessageOutboxPort;
import com.ryuqq.crawlinghub.application.task.port.out.PublishTaskMessagePort;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Task 메시지 이벤트 리스너 (Inbound Adapter)
 *
 * <p>Transaction 커밋 후 SQS로 메시지 발행
 *
 * <p>실패 시 PENDING 상태 유지 → 스케줄러가 재시도
 *
 * <p>Hexagonal Architecture 위치:
 * - Inbound Adapter (Application Layer의 Event를 수신)
 * - Domain Event 구독 및 외부 시스템(SQS)으로 전달
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class TaskMessageEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskMessageEventListener.class);

    private final LoadTaskMessageOutboxPort loadTaskMessageOutboxPort;
    private final PublishTaskMessagePort publishTaskMessagePort;
    private final TaskMessageOutboxManager taskMessageOutboxManager;

    public TaskMessageEventListener(
        LoadTaskMessageOutboxPort loadTaskMessageOutboxPort,
        PublishTaskMessagePort publishTaskMessagePort,
        TaskMessageOutboxManager taskMessageOutboxManager
    ) {
        this.loadTaskMessageOutboxPort = loadTaskMessageOutboxPort;
        this.publishTaskMessagePort = publishTaskMessagePort;
        this.taskMessageOutboxManager = taskMessageOutboxManager;
    }

    /**
     * Transaction 커밋 후 SQS 발행
     *
     * <p>비동기 처리 (메인 트랜잭션과 분리)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMessageCreated(TaskMessageCreatedEvent event) {
        try {
            log.info("Task 메시지 발행 시작. outboxId={}", event.outboxId());

            // 1. Outbox 조회
            TaskMessageOutbox outbox = loadTaskMessageOutboxPort.findById(event.outboxId())
                .orElseThrow(() -> new IllegalArgumentException("Outbox를 찾을 수 없습니다: " + event.outboxId()));

            // 2. SQS 발행
            publishTaskMessagePort.publish(outbox.getTaskId(), outbox.getTaskType());

            // 3. 발행 성공 처리 (SENT)
            taskMessageOutboxManager.markSent(event.outboxId());

            log.info("Task 메시지 발행 완료. outboxId={}, taskId={}, taskType={}",
                event.outboxId(), outbox.getTaskId(), outbox.getTaskType());

        } catch (Exception e) {
            log.error("Task 메시지 발행 실패. outboxId={}, error={}",
                event.outboxId(), e.getMessage(), e);

            // 4. 발행 실패 처리 (PENDING 유지, 재시도 카운트 증가)
            taskMessageOutboxManager.markFailed(event.outboxId(), e.getMessage());
        }
    }
}
