package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 이미지 VO
 *
 * <p>개별 이미지 정보와 S3 업로드 상태를 관리합니다.
 *
 * @param originalUrl 원본 이미지 URL (외부)
 * @param s3Url S3 업로드된 URL (우리 서버, nullable)
 * @param imageType 이미지 타입 (THUMBNAIL, DESCRIPTION)
 * @param status 업로드 상태
 * @param displayOrder 표시 순서 (0부터 시작)
 * @author development-team
 * @since 1.0.0
 */
public record ProductImage(
        String originalUrl,
        String s3Url,
        ImageType imageType,
        ImageUploadStatus status,
        int displayOrder) {

    public ProductImage {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("originalUrl은 필수입니다.");
        }
        if (imageType == null) {
            throw new IllegalArgumentException("imageType은 필수입니다.");
        }
        if (status == null) {
            status = ImageUploadStatus.PENDING;
        }
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder는 0 이상이어야 합니다.");
        }
    }

    /**
     * 신규 이미지 생성 (업로드 대기 상태)
     *
     * @param originalUrl 원본 URL
     * @param imageType 이미지 타입
     * @param displayOrder 표시 순서
     * @return ProductImage
     */
    public static ProductImage ofPending(String originalUrl, ImageType imageType, int displayOrder) {
        return new ProductImage(originalUrl, null, imageType, ImageUploadStatus.PENDING, displayOrder);
    }

    /**
     * 썸네일 이미지 생성
     */
    public static ProductImage thumbnail(String originalUrl, int displayOrder) {
        return ofPending(originalUrl, ImageType.THUMBNAIL, displayOrder);
    }

    /**
     * 상세 설명 이미지 생성
     */
    public static ProductImage description(String originalUrl, int displayOrder) {
        return ofPending(originalUrl, ImageType.DESCRIPTION, displayOrder);
    }

    /**
     * S3 업로드 완료 상태로 변경
     *
     * @param s3Url 업로드된 S3 URL
     * @return 새로운 ProductImage (업로드 완료 상태)
     */
    public ProductImage withS3Uploaded(String s3Url) {
        if (s3Url == null || s3Url.isBlank()) {
            throw new IllegalArgumentException("s3Url은 필수입니다.");
        }
        return new ProductImage(this.originalUrl, s3Url, this.imageType, ImageUploadStatus.UPLOADED, this.displayOrder);
    }

    /**
     * 업로드 진행 중 상태로 변경
     */
    public ProductImage withUploading() {
        return new ProductImage(this.originalUrl, this.s3Url, this.imageType, ImageUploadStatus.UPLOADING, this.displayOrder);
    }

    /**
     * 업로드 실패 상태로 변경
     */
    public ProductImage withFailed() {
        return new ProductImage(this.originalUrl, this.s3Url, this.imageType, ImageUploadStatus.FAILED, this.displayOrder);
    }

    /**
     * 업로드가 필요한지 확인
     */
    public boolean needsUpload() {
        return status == ImageUploadStatus.PENDING || status == ImageUploadStatus.FAILED;
    }

    /**
     * 업로드 완료 여부 확인
     */
    public boolean isUploaded() {
        return status == ImageUploadStatus.UPLOADED && s3Url != null;
    }

    /**
     * 썸네일 이미지인지 확인
     */
    public boolean isThumbnail() {
        return imageType == ImageType.THUMBNAIL;
    }

    /**
     * 상세 설명 이미지인지 확인
     */
    public boolean isDescription() {
        return imageType == ImageType.DESCRIPTION;
    }

    /**
     * 사용할 URL 반환 (S3 URL 우선, 없으면 원본 URL)
     */
    public String getEffectiveUrl() {
        return isUploaded() ? s3Url : originalUrl;
    }
}
