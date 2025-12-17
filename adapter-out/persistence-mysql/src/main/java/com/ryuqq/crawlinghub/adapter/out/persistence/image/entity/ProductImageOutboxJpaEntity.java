package com.ryuqq.crawlinghub.adapter.out.persistence.image.entity;

import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
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
 * ProductImageOutboxJpaEntity - 이미지 업로드 Outbox JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 product_image_outbox 테이블과 매핑됩니다.
 *
 * <p><strong>Outbox 패턴</strong>:
 *
 * <ul>
 *   <li>CrawledProductImage와 같은 트랜잭션에서 저장
 *   <li>별도 스케줄러/이벤트 리스너가 PENDING 상태 Outbox 조회 후 이미지 업로드 실행
 *   <li>업로드 성공 시 COMPLETED로 변경, 실패 시 FAILED로 변경 후 재시도
 *   <li>완료된 Outbox는 정리/삭제 가능
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "product_image_outbox")
public class ProductImageOutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** CrawledProductImage ID (새로운 FK) */
    @Column(name = "crawled_product_image_id")
    private Long crawledProductImageId;

    /** 멱등성 키 (중복 방지) */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    /** 현재 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductOutboxStatus status;

    /** 재시도 횟수 */
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /** 에러 메시지 */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 처리 일시 */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // ===== Legacy 필드 (V9 마이그레이션 중 유지, 추후 삭제 예정) =====

    /**
     * @deprecated V10에서 삭제 예정 - crawled_product_image 테이블로 이동
     */
    @Deprecated
    @Column(name = "crawled_product_id")
    private Long crawledProductId;

    /**
     * @deprecated V10에서 삭제 예정 - crawled_product_image 테이블로 이동
     */
    @Deprecated
    @Column(name = "original_url", length = 2000)
    private String originalUrl;

    /**
     * @deprecated V10에서 삭제 예정 - crawled_product_image 테이블로 이동
     */
    @Deprecated
    @Column(name = "s3_url", length = 2000)
    private String s3Url;

    /**
     * @deprecated V10에서 삭제 예정 - crawled_product_image 테이블로 이동
     */
    @Deprecated
    @Column(name = "image_type", length = 20)
    private String imageType;

    protected ProductImageOutboxJpaEntity() {}

    private ProductImageOutboxJpaEntity(
            Long id,
            Long crawledProductImageId,
            String idempotencyKey,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        this.id = id;
        this.crawledProductImageId = crawledProductImageId;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * @param id ID
     * @param crawledProductImageId 이미지 ID
     * @param idempotencyKey 멱등성 키
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param createdAt 생성 일시
     * @param processedAt 처리 일시
     * @return ProductImageOutboxJpaEntity
     */
    public static ProductImageOutboxJpaEntity of(
            Long id,
            Long crawledProductImageId,
            String idempotencyKey,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new ProductImageOutboxJpaEntity(
                id,
                crawledProductImageId,
                idempotencyKey,
                status,
                retryCount,
                errorMessage,
                createdAt,
                processedAt);
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getCrawledProductImageId() {
        return crawledProductImageId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public ProductOutboxStatus getStatus() {
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

    // Legacy Getters (deprecated)

    /**
     * @deprecated
     */
    @Deprecated
    public Long getCrawledProductId() {
        return crawledProductId;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getOriginalUrl() {
        return originalUrl;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getS3Url() {
        return s3Url;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getImageType() {
        return imageType;
    }
}
