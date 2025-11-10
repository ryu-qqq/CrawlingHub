package com.ryuqq.crawlinghub.domain.product.exception;

/**
 * ProductSyncOutbox를 찾을 수 없을 때 발생하는 도메인 예외
 *
 * <p>발생 조건:
 * <ul>
 *   <li>존재하지 않는 Outbox ID로 조회 시도</li>
 *   <li>이미 삭제된 Outbox 조회 시도</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public class ProductSyncOutboxNotFoundException extends RuntimeException {

    private final Long outboxId;

    public ProductSyncOutboxNotFoundException(Long outboxId) {
        super("ProductSyncOutbox를 찾을 수 없습니다: outboxId=" + outboxId);
        this.outboxId = outboxId;
    }

    public ProductSyncOutboxNotFoundException(Long outboxId, String message) {
        super(message);
        this.outboxId = outboxId;
    }

    public Long getOutboxId() {
        return outboxId;
    }
}
