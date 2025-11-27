package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * Product Outbox 상태
 *
 * <p><strong>상태 전환 규칙</strong>:
 *
 * <pre>
 * PENDING → PROCESSING → COMPLETED
 *              ↓
 *           FAILED → PENDING (재시도)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum ProductOutboxStatus {

    /** 처리 대기 중 */
    PENDING,

    /** 처리 진행 중 */
    PROCESSING,

    /** 처리 완료 */
    COMPLETED,

    /** 처리 실패 (재시도 필요) */
    FAILED;

    /** 대기 상태인지 확인 */
    public boolean isPending() {
        return this == PENDING;
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
