package com.ryuqq.crawlinghub.domain.product.aggregate;

import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Clock;
import java.time.Instant;

/**
 * 크롤링된 상품 이미지
 *
 * <p>CrawledProduct에 속한 이미지 정보를 영구 저장합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>이미지 메타데이터 관리 (원본 URL, S3 URL, 타입)
 *   <li>이미지 표시 순서 관리
 *   <li>S3 업로드 완료 시 URL 업데이트
 *   <li>Fileflow 파일 자산 ID 관리
 * </ul>
 *
 * <p><strong>Outbox와의 관계</strong>:
 *
 * <pre>
 * CrawledProductImage (1) ←── (1) ProductImageOutbox
 * - 이미지 생성 시 Outbox도 함께 생성
 * - Outbox는 업로드 작업 완료 후 삭제 가능
 * - 이미지는 영구 보관
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawledProductImage {

    private final Long id;
    private final CrawledProductId crawledProductId;
    private final String originalUrl;
    private final ImageType imageType;
    private final int displayOrder;
    private String s3Url;
    private String fileAssetId;
    private final Instant createdAt;
    private Instant updatedAt;

    private CrawledProductImage(
            Long id,
            CrawledProductId crawledProductId,
            String originalUrl,
            ImageType imageType,
            int displayOrder,
            String s3Url,
            String fileAssetId,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.crawledProductId = crawledProductId;
        this.originalUrl = originalUrl;
        this.imageType = imageType;
        this.displayOrder = displayOrder;
        this.s3Url = s3Url;
        this.fileAssetId = fileAssetId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 이미지 생성
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 이미지 URL
     * @param imageType 이미지 타입
     * @param displayOrder 표시 순서
     * @param clock 시간 제어
     * @return 새로운 CrawledProductImage
     */
    public static CrawledProductImage forNew(
            CrawledProductId crawledProductId,
            String originalUrl,
            ImageType imageType,
            int displayOrder,
            Clock clock) {
        validateOriginalUrl(originalUrl);
        Instant now = clock.instant();
        return new CrawledProductImage(
                null,
                crawledProductId,
                originalUrl,
                imageType,
                displayOrder,
                null,
                null,
                now,
                null);
    }

    /**
     * 기존 데이터로 복원 (영속성 계층 전용)
     *
     * @param id 이미지 ID
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @param imageType 이미지 타입
     * @param displayOrder 표시 순서
     * @param s3Url S3 URL
     * @param fileAssetId Fileflow 파일 자산 ID
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return 복원된 CrawledProductImage
     */
    public static CrawledProductImage reconstitute(
            Long id,
            CrawledProductId crawledProductId,
            String originalUrl,
            ImageType imageType,
            int displayOrder,
            String s3Url,
            String fileAssetId,
            Instant createdAt,
            Instant updatedAt) {
        return new CrawledProductImage(
                id,
                crawledProductId,
                originalUrl,
                imageType,
                displayOrder,
                s3Url,
                fileAssetId,
                createdAt,
                updatedAt);
    }

    private static void validateOriginalUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("originalUrl은 필수입니다.");
        }
    }

    /**
     * S3 업로드 완료 시 URL 및 파일 자산 ID 설정
     *
     * @param s3Url 업로드된 S3 URL
     * @param fileAssetId Fileflow 파일 자산 ID
     * @param clock 시간 제어
     */
    public void completeUpload(String s3Url, String fileAssetId, Clock clock) {
        if (s3Url == null || s3Url.isBlank()) {
            throw new IllegalArgumentException("s3Url은 필수입니다.");
        }
        this.s3Url = s3Url;
        this.fileAssetId = fileAssetId;
        this.updatedAt = clock.instant();
    }

    /**
     * 업로드 완료 여부 확인
     *
     * @return 업로드 완료 시 true
     */
    public boolean isUploaded() {
        return s3Url != null && !s3Url.isBlank();
    }

    /**
     * 썸네일 이미지인지 확인
     *
     * @return 썸네일이면 true
     */
    public boolean isThumbnail() {
        return imageType == ImageType.THUMBNAIL;
    }

    /**
     * 상세 이미지인지 확인
     *
     * @return 상세 이미지면 true
     */
    public boolean isDescription() {
        return imageType == ImageType.DESCRIPTION;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public CrawledProductId getCrawledProductId() {
        return crawledProductId;
    }

    public Long getCrawledProductIdValue() {
        return crawledProductId.value();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getS3Url() {
        return s3Url;
    }

    public String getFileAssetId() {
        return fileAssetId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
