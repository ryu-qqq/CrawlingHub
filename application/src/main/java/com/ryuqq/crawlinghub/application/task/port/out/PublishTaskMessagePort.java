package com.ryuqq.crawlinghub.application.task.port.out;

import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;

/**
 * Task 메시지 발행 Port (SQS)
 *
 * <p>SQS Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface PublishTaskMessagePort {

    /**
     * Task 메시지를 SQS로 발행
     *
     * @param taskId Task ID
     * @param taskType Task 타입
     * @throws RuntimeException SQS 발행 실패 시
     */
    void publish(TaskId taskId, TaskType taskType);
}
