package com.ryuqq.crawlinghub.adapter.out.persistence.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * SyncOutboxJpaEntity - 외부 동기화 Outbox JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 sync_outbox 테이블과 매핑됩니다.
 *
 * <p><strong>Outbox 패턴 흐름</strong>:
 *
 * <pre>
 * PENDING → PROCESSING → COMPLETED
 *              ↓
 *           FAILED → PENDING (재시도)
 * </pre>
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지
 *   <li>모든 외래키는 Long 타입으로 직접 관리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "sync_outbox")
public class SyncOutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "crawled_product_id", nullable = false)
    private Long crawledProductId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "item_no", nullable = false)
    private Long itemNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false, length = 20)
    private SyncType syncType;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "external_product_id")
    private Long externalProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** JPA 기본 생성자 (protected) */
    protected SyncOutboxJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private SyncOutboxJpaEntity(
            Long id,
            Long crawledProductId,
            Long sellerId,
            Long itemNo,
            SyncType syncType,
            String idempotencyKey,
            Long externalProductId,
            OutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        this.id = id;
        this.crawledProductId = crawledProductId;
        this.sellerId = sellerId;
        this.itemNo = itemNo;
        this.syncType = syncType;
        this.idempotencyKey = idempotencyKey;
        this.externalProductId = externalProductId;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /** of() 스태틱 팩토리 메서드 (Mapper 전용) */
    public static SyncOutboxJpaEntity of(
            Long id,
            Long crawledProductId,
            Long sellerId,
            Long itemNo,
            SyncType syncType,
            String idempotencyKey,
            Long externalProductId,
            OutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new SyncOutboxJpaEntity(
                id,
                crawledProductId,
                sellerId,
                itemNo,
                syncType,
                idempotencyKey,
                externalProductId,
                status,
                retryCount,
                errorMessage,
                createdAt,
                processedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public Long getCrawledProductId() {
        return crawledProductId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getItemNo() {
        return itemNo;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getExternalProductId() {
        return externalProductId;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    /** 동기화 타입 */
    public enum SyncType {
        CREATE,
        UPDATE
    }

    /** Outbox 상태 */
    public enum OutboxStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
