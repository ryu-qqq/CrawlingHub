package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity;

import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
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
 * <p><strong>Outbox 패턴:</strong>
 *
 * <ul>
 *   <li>CrawledProduct와 같은 트랜잭션에서 저장
 *   <li>별도 스케줄러/이벤트 리스너가 PENDING 상태 Outbox 조회 후 이미지 업로드 실행
 *   <li>업로드 성공 시 COMPLETED로 변경, 실패 시 FAILED로 변경 후 재시도
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지
 *   <li>crawledProductId는 Long 타입으로 직접 관리
 * </ul>
 *
 * <p><strong>Lombok 금지:</strong>
 *
 * <ul>
 *   <li>Plain Java getter 사용
 *   <li>Setter 제공 금지
 *   <li>명시적 생성자 제공
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "product_image_outbox")
public class ProductImageOutboxJpaEntity {

    /** 기본 키 (AUTO_INCREMENT) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** CrawledProduct ID (Long FK 전략) */
    @Column(name = "crawled_product_id", nullable = false)
    private Long crawledProductId;

    /** 이미지 타입 (THUMBNAIL/DESCRIPTION) */
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private ImageType imageType;

    /** 원본 이미지 URL */
    @Column(name = "original_url", nullable = false, length = 2000)
    private String originalUrl;

    /** 멱등성 키 (중복 방지) */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    /** 업로드된 S3 URL (완료 시 설정) */
    @Column(name = "s3_url", length = 2000)
    private String s3Url;

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

    /** JPA 기본 생성자 (protected) */
    protected ProductImageOutboxJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private ProductImageOutboxJpaEntity(
            Long id,
            Long crawledProductId,
            ImageType imageType,
            String originalUrl,
            String idempotencyKey,
            String s3Url,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        this.id = id;
        this.crawledProductId = crawledProductId;
        this.imageType = imageType;
        this.originalUrl = originalUrl;
        this.idempotencyKey = idempotencyKey;
        this.s3Url = s3Url;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * <p>Entity 생성은 반드시 이 메서드를 통해서만 가능합니다.
     *
     * <p>Mapper에서 Domain -> Entity 변환 시 사용합니다.
     *
     * @param id ID (null이면 신규)
     * @param crawledProductId CrawledProduct ID
     * @param imageType 이미지 타입
     * @param originalUrl 원본 URL
     * @param idempotencyKey 멱등성 키
     * @param s3Url S3 URL
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param createdAt 생성 일시
     * @param processedAt 처리 일시
     * @return ProductImageOutboxJpaEntity 인스턴스
     */
    public static ProductImageOutboxJpaEntity of(
            Long id,
            Long crawledProductId,
            ImageType imageType,
            String originalUrl,
            String idempotencyKey,
            String s3Url,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new ProductImageOutboxJpaEntity(
                id,
                crawledProductId,
                imageType,
                originalUrl,
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

    public ImageType getImageType() {
        return imageType;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getS3Url() {
        return s3Url;
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
}
