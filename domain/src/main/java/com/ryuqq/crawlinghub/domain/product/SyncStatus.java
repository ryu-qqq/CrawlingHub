package com.ryuqq.crawlinghub.domain.product;

/**
 * 동기화 상태 Enum
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public enum SyncStatus {
    /**
     * 동기화 대기 중
     */
    PENDING,

    /**
     * 동기화 진행 중
     */
    PROCESSING,

    /**
     * 동기화 완료
     */
    COMPLETED,

    /**
     * 동기화 실패
     */
    FAILED
}

