package com.ryuqq.crawlinghub.domain.task.vo;

/**
 * CrawlTask Outbox 상태
 *
 * <p><strong>상태 전환 규칙</strong>:
 *
 * <pre>
 * PENDING → SENT → (삭제)
 *    ↓
 * FAILED → PENDING (재시도)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum OutboxStatus {

    /** 발행 대기 중 */
    PENDING,

    /** 발행 완료 (SQS 전송 성공) */
    SENT,

    /** 발행 실패 (재시도 필요) */
    FAILED;

    /**
     * 발행 대기 상태인지 확인
     *
     * @return PENDING이면 true
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 발행 완료 상태인지 확인
     *
     * @return SENT이면 true
     */
    public boolean isSent() {
        return this == SENT;
    }

    /**
     * 재시도 가능 상태인지 확인
     *
     * @return PENDING 또는 FAILED이면 true
     */
    public boolean canRetry() {
        return this == PENDING || this == FAILED;
    }
}
