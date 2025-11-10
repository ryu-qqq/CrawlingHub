package com.ryuqq.crawlinghub.application.task.port.out;

import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;

/**
 * Task 메시지 Outbox 저장 Port
 *
 * <p>Persistence Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface SaveTaskMessageOutboxPort {

    /**
     * Outbox 저장
     *
     * @param outbox 저장할 TaskMessageOutbox
     * @return 저장된 TaskMessageOutbox (ID 포함)
     */
    TaskMessageOutbox save(TaskMessageOutbox outbox);
}
