package com.ryuqq.crawlinghub.domain.product.event;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ProductSyncOutbox 생성 이벤트
 *
 * <p>역할: ProductSyncOutbox가 생성되었을 때 발행되는 Domain Event
 *
 * <p>사용처:
 * <ul>
 *   <li>ProductManager가 Outbox 저장 후 발행</li>
 *   <li>ExternalSyncEventListener가 구독하여 즉시 처리 (Fast Path)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public class ProductSyncOutboxCreatedEvent {

    private final Long outboxId;
    private final Long productId;
    private final String productJson;
    private final LocalDateTime createdAt;

    public ProductSyncOutboxCreatedEvent(
        Long outboxId,
        Long productId,
        String productJson,
        LocalDateTime createdAt
    ) {
        if (outboxId == null) {
            throw new IllegalArgumentException("Outbox ID는 필수입니다");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID는 필수입니다");
        }
        if (productJson == null || productJson.isBlank()) {
            throw new IllegalArgumentException("Product JSON은 필수입니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }

        this.outboxId = outboxId;
        this.productId = productId;
        this.productJson = productJson;
        this.createdAt = createdAt;
    }

    public Long getOutboxId() {
        return outboxId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductJson() {
        return productJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductSyncOutboxCreatedEvent that = (ProductSyncOutboxCreatedEvent) o;
        return Objects.equals(outboxId, that.outboxId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outboxId);
    }

    @Override
    public String toString() {
        return "ProductSyncOutboxCreatedEvent{" +
            "outboxId=" + outboxId +
            ", productId=" + productId +
            ", createdAt=" + createdAt +
            '}';
    }
}
