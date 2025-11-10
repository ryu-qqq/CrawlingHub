package com.ryuqq.crawlinghub.domain.task.outbox;

/**
 * Task 메시지 Outbox 상태
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public enum TaskMessageStatus {
    /**
     * 발행 대기 (SQS로 아직 발행되지 않음)
     */
    PENDING,

    /**
     * 발행 완료 (SQS로 성공적으로 발행됨)
     */
    SENT
}
