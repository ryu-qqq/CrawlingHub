package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * Product Outbox 상태
 *
 * <p><strong>상태 전환 규칙 (SQS 기반)</strong>:
 *
 * <pre>
 * PENDING → SENT (SQS 발행 성공)
 *    ↓         ↓
 * FAILED   PROCESSING (SQS Consumer 처리 시작)
 *    ↓         ↓
 * PENDING   COMPLETED / FAILED
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum ProductOutboxStatus {

    /** 처리 대기 중 (SQS 발행 전) */
    PENDING,

    /** SQS 발행 완료 (Consumer 처리 대기) */
    SENT,

    /** 처리 진행 중 (Consumer가 처리 시작) */
    PROCESSING,

    /** 처리 완료 */
    COMPLETED,

    /** 처리 실패 (재시도 필요) */
    FAILED;

    /** 대기 상태인지 확인 */
    public boolean isPending() {
        return this == PENDING;
    }

    /** SQS 발행 완료 상태인지 확인 */
    public boolean isSent() {
        return this == SENT;
    }

    /** 진행 중인지 확인 */
    public boolean isProcessing() {
        return this == PROCESSING;
    }

    /** 완료 상태인지 확인 */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /** 실패 상태인지 확인 */
    public boolean isFailed() {
        return this == FAILED;
    }

    /** 재시도 가능 상태인지 확인 */
    public boolean canRetry() {
        return this == PENDING || this == FAILED;
    }
}
