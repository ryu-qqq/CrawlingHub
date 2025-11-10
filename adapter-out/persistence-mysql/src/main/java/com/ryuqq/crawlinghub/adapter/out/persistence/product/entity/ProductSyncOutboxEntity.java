package com.ryuqq.crawlinghub.adapter.out.persistence.product.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ProductSyncOutbox JPA Entity
 *
 * <p>테이블: product_sync_outbox</p>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java</li>
 *   <li>✅ 3-생성자 패턴: no-args, create, reconstitute</li>
 *   <li>✅ Long FK 전략 - productId는 Long 타입</li>
 *   <li>✅ 불변성 - final 필드 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Entity
@Table(
    name = "product_sync_outbox",
    indexes = {
        @Index(name = "idx_status_created", columnList = "status, created_at")
    }
)
public class ProductSyncOutboxEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(name = "product_id", nullable = false)
    private final Long productId;

    @Column(name = "product_json", columnDefinition = "JSON", nullable = false)
    private final String productJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private final SyncStatus status;

    @Column(name = "retry_count", nullable = false)
    private final Integer retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private final String errorMessage;

    @Column(name = "processed_at")
    private final LocalDateTime processedAt;

    /**
     * 동기화 상태 Enum
     */
    public enum SyncStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    /**
     * No-args 생성자 (JPA 필수)
     */
    protected ProductSyncOutboxEntity() {
        super();
        this.id = null;
        this.productId = null;
        this.productJson = null;
        this.status = null;
        this.retryCount = null;
        this.errorMessage = null;
        this.processedAt = null;
    }

    /**
     * 신규 생성용 생성자 (ID 없음)
     */
    protected ProductSyncOutboxEntity(
        Long productId,
        String productJson
    ) {
        super();
        this.id = null;
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.productJson = Objects.requireNonNull(productJson, "productJson must not be null");
        this.status = SyncStatus.PENDING;
        this.retryCount = 0;
        this.errorMessage = null;
        this.processedAt = null;
        initializeAuditFields();
    }

    /**
     * Static Factory Method - 신규 생성
     */
    public static ProductSyncOutboxEntity create(
        Long productId,
        String productJson
    ) {
        return new ProductSyncOutboxEntity(productId, productJson);
    }

    /**
     * DB reconstitute용 전체 생성자
     */
    private ProductSyncOutboxEntity(
        Long id,
        Long productId,
        String productJson,
        SyncStatus status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.productJson = Objects.requireNonNull(productJson, "productJson must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.retryCount = Objects.requireNonNull(retryCount, "retryCount must not be null");
        this.errorMessage = errorMessage;
        this.processedAt = processedAt;
    }

    /**
     * Static Factory Method - DB reconstitute
     */
    public static ProductSyncOutboxEntity reconstitute(
        Long id,
        Long productId,
        String productJson,
        SyncStatus status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductSyncOutboxEntity(
            id, productId, productJson,
            status, retryCount, errorMessage,
            processedAt,
            createdAt, updatedAt
        );
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductJson() {
        return productJson;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductSyncOutboxEntity that = (ProductSyncOutboxEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductSyncOutboxEntity{" +
            "id=" + id +
            ", productId=" + productId +
            ", status=" + status +
            ", retryCount=" + retryCount +
            '}';
    }
}

