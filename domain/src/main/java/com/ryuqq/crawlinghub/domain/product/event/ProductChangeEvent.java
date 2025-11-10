package com.ryuqq.crawlinghub.domain.product.event;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 상품 변경 이벤트
 *
 * <p>역할: 상품 데이터 변경 시 발행되는 Domain Event
 *
 * <p>포함 정보:
 * <ul>
 *   <li>productId: 변경된 상품 ID</li>
 *   <li>source: 변경 소스 (MINI_SHOP, OPTION, DETAIL)</li>
 *   <li>changedFields: 변경된 필드 목록 (필드명 → 변경 내역)</li>
 *   <li>occurredAt: 변경 발생 시각</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public class ProductChangeEvent {

    private final Long productId;
    private final ChangeSource source;
    private final Map<String, FieldChange> changedFields;
    private final LocalDateTime occurredAt;

    public ProductChangeEvent(
        Long productId,
        ChangeSource source,
        Map<String, FieldChange> changedFields,
        LocalDateTime occurredAt
    ) {
        validateRequiredFields(productId, source, changedFields, occurredAt);

        this.productId = productId;
        this.source = source;
        this.changedFields = Map.copyOf(changedFields); // 불변 복사
        this.occurredAt = occurredAt;
    }

    private static void validateRequiredFields(
        Long productId,
        ChangeSource source,
        Map<String, FieldChange> changedFields,
        LocalDateTime occurredAt
    ) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID는 필수입니다");
        }
        if (source == null) {
            throw new IllegalArgumentException("변경 소스는 필수입니다");
        }
        if (changedFields == null || changedFields.isEmpty()) {
            throw new IllegalArgumentException("변경 필드는 최소 1개 이상이어야 합니다");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("변경 발생 시각은 필수입니다");
        }
    }

    public Long getProductId() {
        return productId;
    }

    public ChangeSource getSource() {
        return source;
    }

    public Map<String, FieldChange> getChangedFields() {
        return changedFields;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * 특정 필드가 변경되었는지 확인
     */
    public boolean hasFieldChanged(String fieldName) {
        return changedFields.containsKey(fieldName);
    }

    /**
     * 변경된 필드 수
     */
    public int getChangedFieldCount() {
        return changedFields.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductChangeEvent that = (ProductChangeEvent) o;
        return Objects.equals(productId, that.productId) &&
            source == that.source &&
            Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, source, occurredAt);
    }

    @Override
    public String toString() {
        return "ProductChangeEvent{" +
            "productId=" + productId +
            ", source=" + source +
            ", changedFieldCount=" + changedFields.size() +
            ", occurredAt=" + occurredAt +
            '}';
    }
}

