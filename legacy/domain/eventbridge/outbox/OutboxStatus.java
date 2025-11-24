package com.ryuqq.crawlinghub.domain.eventbridge.outbox;

/** Outbox 이벤트 상태 값 */
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
