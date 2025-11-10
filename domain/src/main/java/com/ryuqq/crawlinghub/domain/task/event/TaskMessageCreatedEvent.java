package com.ryuqq.crawlinghub.domain.task.event;

/**
 * Task 메시지 생성 Domain Event
 *
 * <p>TaskMessageOutbox가 생성될 때 발행되는 Domain Event
 *
 * <p>Transaction 커밋 후 SQS 발행을 위한 이벤트
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record TaskMessageCreatedEvent(Long outboxId) {

    public TaskMessageCreatedEvent {
        if (outboxId == null || outboxId <= 0) {
            throw new IllegalArgumentException("outboxId는 필수이며 양수여야 합니다");
        }
    }
}
