package com.ryuqq.crawlinghub.adapter.out.persistence.image.entity;

import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
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
 * CrawledProductImageJpaEntity - 크롤링 상품 이미지 JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawled_product_image 테이블과 매핑됩니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>이미지 메타데이터 영구 저장
 *   <li>원본 URL, S3 URL, 이미지 타입 관리
 *   <li>표시 순서 관리
 *   <li>Fileflow 파일 자산 ID 관리
 * </ul>
 *
 * <p><strong>Long FK 전략</strong>:
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지
 *   <li>crawledProductId는 Long 타입으로 직접 관리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "crawled_product_image")
public class CrawledProductImageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "crawled_product_id", nullable = false)
    private Long crawledProductId;

    @Column(name = "original_url", nullable = false, length = 2000)
    private String originalUrl;

    @Column(name = "s3_url", length = 2000)
    private String s3Url;

    @Column(name = "file_asset_id", length = 100)
    private String fileAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private ImageType imageType;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected CrawledProductImageJpaEntity() {}

    private CrawledProductImageJpaEntity(
            Long id,
            Long crawledProductId,
            String originalUrl,
            String s3Url,
            String fileAssetId,
            ImageType imageType,
            int displayOrder,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.crawledProductId = crawledProductId;
        this.originalUrl = originalUrl;
        this.s3Url = s3Url;
        this.fileAssetId = fileAssetId;
        this.imageType = imageType;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * @param id ID
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @param s3Url S3 URL
     * @param fileAssetId Fileflow 파일 자산 ID
     * @param imageType 이미지 타입
     * @param displayOrder 표시 순서
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return CrawledProductImageJpaEntity
     */
    public static CrawledProductImageJpaEntity of(
            Long id,
            Long crawledProductId,
            String originalUrl,
            String s3Url,
            String fileAssetId,
            ImageType imageType,
            int displayOrder,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CrawledProductImageJpaEntity(
                id,
                crawledProductId,
                originalUrl,
                s3Url,
                fileAssetId,
                imageType,
                displayOrder,
                createdAt,
                updatedAt);
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getCrawledProductId() {
        return crawledProductId;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getS3Url() {
        return s3Url;
    }

    public String getFileAssetId() {
        return fileAssetId;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
