package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.port.out.LoadTaskMessageOutboxPort;
import com.ryuqq.crawlinghub.application.task.port.out.SaveTaskMessageOutboxPort;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import com.ryuqq.crawlinghub.domain.task.event.TaskMessageCreatedEvent;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Task 메시지 Outbox 관리 매니저
 *
 * <p>Task 메시지의 생성, 발행 상태 관리를 담당
 *
 * <p>Domain Event 처리 (Application Layer 패턴):
 * - TaskMessageOutbox는 순수 Domain 객체 (Framework 독립적)
 * - Application Layer에서 ApplicationEventPublisher로 Event 발행
 * - TaskMessageEventListener가 Event 구독하여 SQS 발행
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class TaskMessageOutboxManager {

    private static final Logger log = LoggerFactory.getLogger(TaskMessageOutboxManager.class);

    private final LoadTaskMessageOutboxPort loadTaskMessageOutboxPort;
    private final SaveTaskMessageOutboxPort saveTaskMessageOutboxPort;
    private final ApplicationEventPublisher eventPublisher;

    public TaskMessageOutboxManager(
        LoadTaskMessageOutboxPort loadTaskMessageOutboxPort,
        SaveTaskMessageOutboxPort saveTaskMessageOutboxPort,
        ApplicationEventPublisher eventPublisher
    ) {
        this.loadTaskMessageOutboxPort = loadTaskMessageOutboxPort;
        this.saveTaskMessageOutboxPort = saveTaskMessageOutboxPort;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Task 메시지 Outbox 생성 및 저장 (PENDING 상태)
     *
     * <p>Application Layer Event 발행 패턴:
     * - Outbox 내부에서 Event 생성 (createEvent())
     * - Event가 null이 아니면 ApplicationEventPublisher로 발행
     * - TaskMessageEventListener가 Event 구독하여 SQS 발행
     * - Transaction 커밋 후 (@TransactionalEventListener AFTER_COMMIT)
     */
    @Transactional
    public void createTaskMessage(TaskId taskId, TaskType taskType) {
        // 1. Outbox 생성 (PENDING)
        TaskMessageOutbox outbox = TaskMessageOutbox.createPending(taskId, taskType);

        // 2. 저장
        TaskMessageOutbox savedOutbox = saveTaskMessageOutboxPort.save(outbox);

        // 3. Outbox에서 Event 생성
        TaskMessageCreatedEvent event = savedOutbox.createEvent();

        // 4. Event가 null이 아니면 발행
        if (event != null) {
            eventPublisher.publishEvent(event);
            log.debug("Task 메시지 Outbox 생성 및 Event 발행. outboxId={}, taskId={}, taskType={}",
                savedOutbox.getOutboxId(), taskId.value(), taskType);
        } else {
            log.warn("Task 메시지 Outbox 생성했으나 Event 발행 불가 (상태가 PENDING이 아님). outboxId={}, taskId={}, status={}",
                savedOutbox.getOutboxId(), taskId.value(), savedOutbox.getStatus());
        }
    }

    /**
     * 여러 Task 메시지 Outbox 생성 및 저장 (Bulk)
     *
     * <p>META Task가 MINI_SHOP Task들을 동적 생성할 때 사용
     *
     * <p>Application Layer Event 발행 패턴:
     * - Outbox 내부에서 Event 생성 (createEvent())
     * - Event가 null이 아니면 ApplicationEventPublisher로 발행
     * - TaskMessageEventListener가 Event 구독하여 SQS 발행
     *
     * @param tasks 메시지로 발행할 Task 목록
     */
    @Transactional
    public void publishTaskMessages(List<Task> tasks) {
        log.info("Task 메시지 대량 생성 시작. 생성할 Task 수: {}", tasks.size());

        for (Task task : tasks) {
            // 1. Outbox 생성 (PENDING)
            TaskMessageOutbox outbox = TaskMessageOutbox.createPending(
                TaskId.of(task.getIdValue()),
                task.getTaskType()
            );

            // 2. 저장
            TaskMessageOutbox savedOutbox = saveTaskMessageOutboxPort.save(outbox);

            // 3. Outbox에서 Event 생성
            TaskMessageCreatedEvent event = savedOutbox.createEvent();

            // 4. Event가 null이 아니면 발행
            if (event != null) {
                eventPublisher.publishEvent(event);
                log.debug("Task 메시지 Outbox 생성 및 Event 발행. taskId={}, taskType={}, outboxId={}",
                    task.getIdValue(), task.getTaskType(), savedOutbox.getOutboxId());
            } else {
                log.warn("Task 메시지 Outbox 생성했으나 Event 발행 불가 (상태가 PENDING이 아님). outboxId={}, taskId={}, status={}",
                    savedOutbox.getOutboxId(), task.getIdValue(), savedOutbox.getStatus());
            }
        }

        log.info("Task 메시지 대량 생성 완료. 생성된 Outbox 수: {}", tasks.size());
    }

    /**
     * Task 메시지 발행 성공 처리
     */
    @Transactional
    public void markSent(Long outboxId) {
        TaskMessageOutbox outbox = loadTaskMessageOutboxPort.findById(outboxId)
            .orElseThrow(() -> new IllegalArgumentException("Outbox를 찾을 수 없습니다: " + outboxId));

        outbox.markSent();
        saveTaskMessageOutboxPort.save(outbox);
    }

    /**
     * Task 메시지 발행 실패 처리
     */
    @Transactional
    public void markFailed(Long outboxId, String errorMessage) {
        TaskMessageOutbox outbox = loadTaskMessageOutboxPort.findById(outboxId)
            .orElseThrow(() -> new IllegalArgumentException("Outbox를 찾을 수 없습니다: " + outboxId));

        outbox.markFailed(errorMessage);
        saveTaskMessageOutboxPort.save(outbox);
    }

    /**
     * PENDING 상태의 메시지 목록 조회 (스케줄러용)
     *
     * <p>비관적 락 적용:
     * - TaskMessageScheduler가 동시 실행될 때 중복 처리 방지
     * - QueryDSL PESSIMISTIC_WRITE 락 사용
     */
    @Transactional
    public List<TaskMessageOutbox> findPendingMessages() {
        return loadTaskMessageOutboxPort.findByStatusWithLock(TaskMessageStatus.PENDING);
    }
}
