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
 * ImageOutboxJpaEntity - 이미지 업로드 Outbox JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 image_outbox 테이블과 매핑됩니다.
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
@Table(name = "image_outbox")
public class ImageOutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "crawled_product_id", nullable = false)
    private Long crawledProductId;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private ImageType imageType;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "s3_url", length = 2048)
    private String s3Url;

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
    protected ImageOutboxJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private ImageOutboxJpaEntity(
            Long id,
            Long crawledProductId,
            String originalUrl,
            ImageType imageType,
            String idempotencyKey,
            String s3Url,
            OutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        this.id = id;
        this.crawledProductId = crawledProductId;
        this.originalUrl = originalUrl;
        this.imageType = imageType;
        this.idempotencyKey = idempotencyKey;
        this.s3Url = s3Url;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /** of() 스태틱 팩토리 메서드 (Mapper 전용) */
    public static ImageOutboxJpaEntity of(
            Long id,
            Long crawledProductId,
            String originalUrl,
            ImageType imageType,
            String idempotencyKey,
            String s3Url,
            OutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new ImageOutboxJpaEntity(
                id,
                crawledProductId,
                originalUrl,
                imageType,
                idempotencyKey,
                s3Url,
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

    public String getOriginalUrl() {
        return originalUrl;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getS3Url() {
        return s3Url;
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

    /** 이미지 타입 */
    public enum ImageType {
        THUMBNAIL,
        DESCRIPTION
    }

    /** Outbox 상태 */
    public enum OutboxStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
