package com.ryuqq.crawlinghub.domain.product.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductImage 도메인 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("domain")
@DisplayName("CrawledProductImage 단위 테스트")
class CrawledProductImageTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final String ORIGINAL_URL = "https://example.com/image.jpg";
    private static final String S3_URL = "https://s3.amazonaws.com/bucket/image.jpg";
    private static final String FILE_ASSET_ID = "asset-uuid-123";

    @Nested
    @DisplayName("forNew() 테스트")
    class ForNewTests {

        @Test
        @DisplayName("성공 - 신규 썸네일 이미지 생성")
        void shouldCreateNewThumbnailImage() {
            // When
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);

            // Then
            assertThat(image.getId()).isNull();
            assertThat(image.getCrawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(image.getCrawledProductIdValue()).isEqualTo(1L);
            assertThat(image.getOriginalUrl()).isEqualTo(ORIGINAL_URL);
            assertThat(image.getImageType()).isEqualTo(ImageType.THUMBNAIL);
            assertThat(image.getDisplayOrder()).isZero();
            assertThat(image.getS3Url()).isNull();
            assertThat(image.getFileAssetId()).isNull();
            assertThat(image.getCreatedAt()).isEqualTo(FIXED_INSTANT);
            assertThat(image.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - 신규 상세 이미지 생성")
        void shouldCreateNewDescriptionImage() {
            // When
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.DESCRIPTION, 5, FIXED_INSTANT);

            // Then
            assertThat(image.getImageType()).isEqualTo(ImageType.DESCRIPTION);
            assertThat(image.getDisplayOrder()).isEqualTo(5);
        }

        @Test
        @DisplayName("실패 - originalUrl이 null인 경우")
        void shouldThrowExceptionWhenOriginalUrlIsNull() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    CrawledProductImage.forNew(
                                            PRODUCT_ID,
                                            null,
                                            ImageType.THUMBNAIL,
                                            0,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("originalUrl은 필수");
        }

        @Test
        @DisplayName("실패 - originalUrl이 빈 문자열인 경우")
        void shouldThrowExceptionWhenOriginalUrlIsBlank() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    CrawledProductImage.forNew(
                                            PRODUCT_ID,
                                            "  ",
                                            ImageType.THUMBNAIL,
                                            0,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("originalUrl은 필수");
        }
    }

    @Nested
    @DisplayName("reconstitute() 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("성공 - 업로드 전 상태 복원")
        void shouldReconstituteWithPendingState() {
            // When
            CrawledProductImage image =
                    CrawledProductImage.reconstitute(
                            1L,
                            PRODUCT_ID,
                            ORIGINAL_URL,
                            ImageType.THUMBNAIL,
                            0,
                            null,
                            null,
                            FIXED_INSTANT,
                            null);

            // Then
            assertThat(image.getId()).isEqualTo(1L);
            assertThat(image.getS3Url()).isNull();
            assertThat(image.getFileAssetId()).isNull();
            assertThat(image.isUploaded()).isFalse();
        }

        @Test
        @DisplayName("성공 - 업로드 완료 상태 복원")
        void shouldReconstituteWithUploadedState() {
            // When
            CrawledProductImage image =
                    CrawledProductImage.reconstitute(
                            1L,
                            PRODUCT_ID,
                            ORIGINAL_URL,
                            ImageType.THUMBNAIL,
                            0,
                            S3_URL,
                            FILE_ASSET_ID,
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // Then
            assertThat(image.getS3Url()).isEqualTo(S3_URL);
            assertThat(image.getFileAssetId()).isEqualTo(FILE_ASSET_ID);
            assertThat(image.isUploaded()).isTrue();
            assertThat(image.getUpdatedAt()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("completeUpload() 테스트")
    class CompleteUploadTests {

        @Test
        @DisplayName("성공 - S3 업로드 완료 처리")
        void shouldCompleteUpload() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);

            Instant laterInstant = Instant.parse("2025-01-01T01:00:00Z");

            // When
            image.completeUpload(S3_URL, FILE_ASSET_ID, laterInstant);

            // Then
            assertThat(image.getS3Url()).isEqualTo(S3_URL);
            assertThat(image.getFileAssetId()).isEqualTo(FILE_ASSET_ID);
            assertThat(image.isUploaded()).isTrue();
            assertThat(image.getUpdatedAt()).isEqualTo(Instant.parse("2025-01-01T01:00:00Z"));
        }

        @Test
        @DisplayName("성공 - fileAssetId가 null이어도 업로드 완료 가능")
        void shouldCompleteUploadWithoutFileAssetId() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);

            // When
            image.completeUpload(S3_URL, null, FIXED_INSTANT);

            // Then
            assertThat(image.getS3Url()).isEqualTo(S3_URL);
            assertThat(image.getFileAssetId()).isNull();
            assertThat(image.isUploaded()).isTrue();
        }

        @Test
        @DisplayName("실패 - s3Url이 null인 경우")
        void shouldThrowExceptionWhenS3UrlIsNull() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);

            // When & Then
            assertThatThrownBy(() -> image.completeUpload(null, FILE_ASSET_ID, FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("s3Url은 필수");
        }

        @Test
        @DisplayName("실패 - s3Url이 빈 문자열인 경우")
        void shouldThrowExceptionWhenS3UrlIsBlank() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);

            // When & Then
            assertThatThrownBy(() -> image.completeUpload("  ", FILE_ASSET_ID, FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("s3Url은 필수");
        }
    }

    @Nested
    @DisplayName("이미지 타입 확인 테스트")
    class ImageTypeTests {

        @Test
        @DisplayName("썸네일 이미지 확인")
        void shouldIdentifyThumbnailImage() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);

            // Then
            assertThat(image.isThumbnail()).isTrue();
            assertThat(image.isDescription()).isFalse();
        }

        @Test
        @DisplayName("상세 이미지 확인")
        void shouldIdentifyDescriptionImage() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.DESCRIPTION, 0, FIXED_INSTANT);

            // Then
            assertThat(image.isThumbnail()).isFalse();
            assertThat(image.isDescription()).isTrue();
        }
    }

    @Nested
    @DisplayName("isUploaded() 테스트")
    class IsUploadedTests {

        @Test
        @DisplayName("업로드 전 - false 반환")
        void shouldReturnFalseWhenNotUploaded() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);

            // Then
            assertThat(image.isUploaded()).isFalse();
        }

        @Test
        @DisplayName("업로드 후 - true 반환")
        void shouldReturnTrueWhenUploaded() {
            // Given
            CrawledProductImage image =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_INSTANT);
            image.completeUpload(S3_URL, FILE_ASSET_ID, FIXED_INSTANT);

            // Then
            assertThat(image.isUploaded()).isTrue();
        }
    }
}
