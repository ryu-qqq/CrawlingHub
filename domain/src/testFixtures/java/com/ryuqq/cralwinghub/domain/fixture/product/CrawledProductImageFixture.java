package com.ryuqq.cralwinghub.domain.fixture.product;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Instant;

/**
 * CrawledProductImage 테스트 Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawledProductImageFixture {

    private CrawledProductImageFixture() {}

    /**
     * 신규 썸네일 이미지 생성 (업로드 전)
     *
     * @return CrawledProductImage
     */
    public static CrawledProductImage aThumbnailImage() {
        return CrawledProductImage.forNew(
                CrawledProductId.of(1L),
                "https://example.com/thumbnail.jpg",
                ImageType.THUMBNAIL,
                0,
                Instant.now());
    }

    /**
     * 신규 상세 이미지 생성 (업로드 전)
     *
     * @return CrawledProductImage
     */
    public static CrawledProductImage aDescriptionImage() {
        return CrawledProductImage.forNew(
                CrawledProductId.of(1L),
                "https://example.com/detail.jpg",
                ImageType.DESCRIPTION,
                0,
                Instant.now());
    }

    /**
     * 복원된 썸네일 이미지 (업로드 전)
     *
     * @return CrawledProductImage
     */
    public static CrawledProductImage aReconstitutedThumbnailPending() {
        return CrawledProductImage.reconstitute(
                1L,
                CrawledProductId.of(1L),
                "https://example.com/thumbnail.jpg",
                ImageType.THUMBNAIL,
                0,
                null,
                null,
                Instant.now(),
                null);
    }

    /**
     * 복원된 썸네일 이미지 (업로드 완료)
     *
     * @return CrawledProductImage
     */
    public static CrawledProductImage aReconstitutedThumbnailUploaded() {
        return CrawledProductImage.reconstitute(
                1L,
                CrawledProductId.of(1L),
                "https://example.com/thumbnail.jpg",
                ImageType.THUMBNAIL,
                0,
                "https://s3.amazonaws.com/bucket/thumbnail.jpg",
                "file-asset-id-123",
                Instant.now(),
                Instant.now());
    }

    /**
     * 복원된 상세 이미지 (업로드 완료)
     *
     * @return CrawledProductImage
     */
    public static CrawledProductImage aReconstitutedDescriptionUploaded() {
        return CrawledProductImage.reconstitute(
                2L,
                CrawledProductId.of(1L),
                "https://example.com/detail.jpg",
                ImageType.DESCRIPTION,
                1,
                "https://s3.amazonaws.com/bucket/detail.jpg",
                "file-asset-id-456",
                Instant.now(),
                Instant.now());
    }

    /**
     * 특정 ID와 URL로 복원된 이미지 (업로드 전)
     *
     * @param id 이미지 ID
     * @param crawledProductId 상품 ID
     * @param originalUrl 원본 URL
     * @param imageType 이미지 타입
     * @param displayOrder 표시 순서
     * @return CrawledProductImage
     */
    public static CrawledProductImage aReconstitutedPending(
            Long id,
            Long crawledProductId,
            String originalUrl,
            ImageType imageType,
            int displayOrder) {
        return CrawledProductImage.reconstitute(
                id,
                CrawledProductId.of(crawledProductId),
                originalUrl,
                imageType,
                displayOrder,
                null,
                null,
                Instant.now(),
                null);
    }

    /**
     * 특정 ID와 URL로 복원된 이미지 (업로드 완료)
     *
     * @param id 이미지 ID
     * @param crawledProductId 상품 ID
     * @param originalUrl 원본 URL
     * @param s3Url S3 URL
     * @param imageType 이미지 타입
     * @param displayOrder 표시 순서
     * @return CrawledProductImage
     */
    public static CrawledProductImage aReconstitutedUploaded(
            Long id,
            Long crawledProductId,
            String originalUrl,
            String s3Url,
            ImageType imageType,
            int displayOrder) {
        return CrawledProductImage.reconstitute(
                id,
                CrawledProductId.of(crawledProductId),
                originalUrl,
                imageType,
                displayOrder,
                s3Url,
                null,
                Instant.now(),
                Instant.now());
    }
}
