package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ProductImage VO 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProductImage 테스트")
class ProductImageTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 ProductImage 생성")
        void shouldCreateWithValidValues() {
            // given & when
            ProductImage image =
                    new ProductImage(
                            "https://example.com/img.jpg",
                            null,
                            ImageType.THUMBNAIL,
                            ImageUploadStatus.PENDING,
                            0);

            // then
            assertThat(image.originalUrl()).isEqualTo("https://example.com/img.jpg");
            assertThat(image.s3Url()).isNull();
            assertThat(image.imageType()).isEqualTo(ImageType.THUMBNAIL);
            assertThat(image.status()).isEqualTo(ImageUploadStatus.PENDING);
            assertThat(image.displayOrder()).isZero();
        }

        @Test
        @DisplayName("status가 null이면 PENDING으로 기본값 설정")
        void shouldDefaultStatusToPending() {
            // given & when
            ProductImage image =
                    new ProductImage(
                            "https://example.com/img.jpg", null, ImageType.THUMBNAIL, null, 0);

            // then
            assertThat(image.status()).isEqualTo(ImageUploadStatus.PENDING);
        }

        @Test
        @DisplayName("originalUrl이 null이면 예외 발생")
        void shouldThrowWhenOriginalUrlIsNull() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    new ProductImage(
                                            null,
                                            null,
                                            ImageType.THUMBNAIL,
                                            ImageUploadStatus.PENDING,
                                            0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("originalUrl은 필수");
        }

        @Test
        @DisplayName("originalUrl이 빈 문자열이면 예외 발생")
        void shouldThrowWhenOriginalUrlIsBlank() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    new ProductImage(
                                            "  ",
                                            null,
                                            ImageType.THUMBNAIL,
                                            ImageUploadStatus.PENDING,
                                            0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("originalUrl은 필수");
        }

        @Test
        @DisplayName("imageType이 null이면 예외 발생")
        void shouldThrowWhenImageTypeIsNull() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    new ProductImage(
                                            "https://example.com/img.jpg",
                                            null,
                                            null,
                                            ImageUploadStatus.PENDING,
                                            0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("imageType은 필수");
        }

        @Test
        @DisplayName("displayOrder가 음수면 예외 발생")
        void shouldThrowWhenDisplayOrderIsNegative() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    new ProductImage(
                                            "https://example.com/img.jpg",
                                            null,
                                            ImageType.THUMBNAIL,
                                            ImageUploadStatus.PENDING,
                                            -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("displayOrder는 0 이상");
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("PENDING 상태로 이미지 생성")
        void shouldCreatePendingImage() {
            // given & when
            ProductImage image =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0);

            // then
            assertThat(image.status()).isEqualTo(ImageUploadStatus.PENDING);
            assertThat(image.s3Url()).isNull();
        }

        @Test
        @DisplayName("썸네일 이미지 생성")
        void shouldCreateThumbnailImage() {
            // given & when
            ProductImage image = ProductImage.thumbnail("https://example.com/img.jpg", 0);

            // then
            assertThat(image.imageType()).isEqualTo(ImageType.THUMBNAIL);
            assertThat(image.status()).isEqualTo(ImageUploadStatus.PENDING);
        }

        @Test
        @DisplayName("상세 설명 이미지 생성")
        void shouldCreateDescriptionImage() {
            // given & when
            ProductImage image = ProductImage.description("https://example.com/img.jpg", 1);

            // then
            assertThat(image.imageType()).isEqualTo(ImageType.DESCRIPTION);
            assertThat(image.displayOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("상태 변경 메서드 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("S3 업로드 완료 상태로 변경")
        void shouldTransitionToUploaded() {
            // given
            ProductImage pending =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0);

            // when
            ProductImage uploaded = pending.withS3Uploaded("https://s3.example.com/img.jpg");

            // then
            assertThat(uploaded.status()).isEqualTo(ImageUploadStatus.UPLOADED);
            assertThat(uploaded.s3Url()).isEqualTo("https://s3.example.com/img.jpg");
            assertThat(uploaded.originalUrl()).isEqualTo("https://example.com/img.jpg");
        }

        @Test
        @DisplayName("S3 URL이 null이면 예외 발생")
        void shouldThrowWhenS3UrlIsNull() {
            // given
            ProductImage pending =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0);

            // when & then
            assertThatThrownBy(() -> pending.withS3Uploaded(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("s3Url은 필수");
        }

        @Test
        @DisplayName("업로드 중 상태로 변경")
        void shouldTransitionToUploading() {
            // given
            ProductImage pending =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0);

            // when
            ProductImage uploading = pending.withUploading();

            // then
            assertThat(uploading.status()).isEqualTo(ImageUploadStatus.UPLOADING);
        }

        @Test
        @DisplayName("업로드 실패 상태로 변경")
        void shouldTransitionToFailed() {
            // given
            ProductImage uploading =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0)
                            .withUploading();

            // when
            ProductImage failed = uploading.withFailed();

            // then
            assertThat(failed.status()).isEqualTo(ImageUploadStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StatusCheckTest {

        @Test
        @DisplayName("업로드가 필요한지 확인")
        void shouldCheckNeedsUpload() {
            // given
            ProductImage pending =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0);
            ProductImage failed = pending.withFailed();
            ProductImage uploaded = pending.withS3Uploaded("https://s3.example.com/img.jpg");

            // when & then
            assertThat(pending.needsUpload()).isTrue();
            assertThat(failed.needsUpload()).isTrue();
            assertThat(uploaded.needsUpload()).isFalse();
        }

        @Test
        @DisplayName("업로드 완료 여부 확인")
        void shouldCheckIsUploaded() {
            // given
            ProductImage pending =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0);
            ProductImage uploaded = pending.withS3Uploaded("https://s3.example.com/img.jpg");

            // when & then
            assertThat(pending.isUploaded()).isFalse();
            assertThat(uploaded.isUploaded()).isTrue();
        }

        @Test
        @DisplayName("썸네일 이미지인지 확인")
        void shouldCheckIsThumbnail() {
            // given
            ProductImage thumbnail = ProductImage.thumbnail("https://example.com/img.jpg", 0);
            ProductImage description = ProductImage.description("https://example.com/img.jpg", 0);

            // when & then
            assertThat(thumbnail.isThumbnail()).isTrue();
            assertThat(description.isThumbnail()).isFalse();
        }

        @Test
        @DisplayName("상세 설명 이미지인지 확인")
        void shouldCheckIsDescription() {
            // given
            ProductImage thumbnail = ProductImage.thumbnail("https://example.com/img.jpg", 0);
            ProductImage description = ProductImage.description("https://example.com/img.jpg", 0);

            // when & then
            assertThat(thumbnail.isDescription()).isFalse();
            assertThat(description.isDescription()).isTrue();
        }

        @Test
        @DisplayName("효과적인 URL 반환")
        void shouldReturnEffectiveUrl() {
            // given
            ProductImage pending =
                    ProductImage.ofPending("https://example.com/img.jpg", ImageType.THUMBNAIL, 0);
            ProductImage uploaded = pending.withS3Uploaded("https://s3.example.com/img.jpg");

            // when & then
            assertThat(pending.getEffectiveUrl()).isEqualTo("https://example.com/img.jpg");
            assertThat(uploaded.getEffectiveUrl()).isEqualTo("https://s3.example.com/img.jpg");
        }
    }
}
