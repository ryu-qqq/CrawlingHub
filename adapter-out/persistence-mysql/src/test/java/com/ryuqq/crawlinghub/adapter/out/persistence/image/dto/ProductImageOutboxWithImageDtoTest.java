package com.ryuqq.crawlinghub.adapter.out.persistence.image.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ProductImageOutboxWithImageDto 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@DisplayName("ProductImageOutboxWithImageDto 단위 테스트")
class ProductImageOutboxWithImageDtoTest {

    @Test
    @DisplayName("성공 - DTO 생성 및 필드 접근")
    void shouldCreateDtoAndAccessFields() {
        // Given
        Long id = 1L;
        Long crawledProductImageId = 100L;
        String idempotencyKey = "image-100-1234567890";
        ProductOutboxStatus status = ProductOutboxStatus.PENDING;
        int retryCount = 0;
        String errorMessage = null;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime processedAt = null;
        Long crawledProductId = 50L;
        String originalUrl = "https://example.com/image.jpg";
        String s3Url = "https://s3.example.com/image.jpg";
        ImageType imageType = ImageType.THUMBNAIL;

        // When
        ProductImageOutboxWithImageDto dto =
                new ProductImageOutboxWithImageDto(
                        id,
                        crawledProductImageId,
                        idempotencyKey,
                        status,
                        retryCount,
                        errorMessage,
                        createdAt,
                        processedAt,
                        crawledProductId,
                        originalUrl,
                        s3Url,
                        imageType);

        // Then
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.crawledProductImageId()).isEqualTo(crawledProductImageId);
        assertThat(dto.idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(dto.status()).isEqualTo(status);
        assertThat(dto.retryCount()).isEqualTo(retryCount);
        assertThat(dto.errorMessage()).isNull();
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.processedAt()).isNull();
        assertThat(dto.crawledProductId()).isEqualTo(crawledProductId);
        assertThat(dto.originalUrl()).isEqualTo(originalUrl);
        assertThat(dto.s3Url()).isEqualTo(s3Url);
        assertThat(dto.imageType()).isEqualTo(imageType);
    }

    @Test
    @DisplayName("성공 - 재시도 가능 여부 확인 (재시도 횟수 0)")
    void shouldReturnCanRetryTrueWhenRetryCountIsZero() {
        // Given
        ProductImageOutboxWithImageDto dto = createDtoWithRetryCount(0);

        // When
        boolean result = dto.canRetry();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공 - 재시도 가능 여부 확인 (재시도 횟수 2)")
    void shouldReturnCanRetryTrueWhenRetryCountIsTwo() {
        // Given
        ProductImageOutboxWithImageDto dto = createDtoWithRetryCount(2);

        // When
        boolean result = dto.canRetry();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공 - 재시도 불가능 확인 (재시도 횟수 3)")
    void shouldReturnCanRetryFalseWhenRetryCountIsThree() {
        // Given
        ProductImageOutboxWithImageDto dto = createDtoWithRetryCount(3);

        // When
        boolean result = dto.canRetry();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("성공 - 재시도 불가능 확인 (재시도 횟수 초과)")
    void shouldReturnCanRetryFalseWhenRetryCountExceedsLimit() {
        // Given
        ProductImageOutboxWithImageDto dto = createDtoWithRetryCount(5);

        // When
        boolean result = dto.canRetry();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("성공 - 실패 상태 DTO 생성")
    void shouldCreateFailedDto() {
        // Given
        ProductImageOutboxWithImageDto dto =
                new ProductImageOutboxWithImageDto(
                        1L,
                        100L,
                        "image-100-1234567890",
                        ProductOutboxStatus.FAILED,
                        3,
                        "Upload failed: Network error",
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now(),
                        50L,
                        "https://example.com/image.jpg",
                        null,
                        ImageType.DESCRIPTION);

        // Then
        assertThat(dto.status()).isEqualTo(ProductOutboxStatus.FAILED);
        assertThat(dto.retryCount()).isEqualTo(3);
        assertThat(dto.errorMessage()).isEqualTo("Upload failed: Network error");
        assertThat(dto.processedAt()).isNotNull();
        assertThat(dto.s3Url()).isNull();
        assertThat(dto.imageType()).isEqualTo(ImageType.DESCRIPTION);
        assertThat(dto.canRetry()).isFalse();
    }

    @Test
    @DisplayName("성공 - nullable 필드 처리")
    void shouldHandleNullableFields() {
        // Given
        ProductImageOutboxWithImageDto dto =
                new ProductImageOutboxWithImageDto(
                        1L,
                        100L,
                        "image-100-1234567890",
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        LocalDateTime.now(),
                        null,
                        null,
                        null,
                        null,
                        null);

        // Then
        assertThat(dto.errorMessage()).isNull();
        assertThat(dto.processedAt()).isNull();
        assertThat(dto.crawledProductId()).isNull();
        assertThat(dto.originalUrl()).isNull();
        assertThat(dto.s3Url()).isNull();
        assertThat(dto.imageType()).isNull();
    }

    private ProductImageOutboxWithImageDto createDtoWithRetryCount(int retryCount) {
        return new ProductImageOutboxWithImageDto(
                1L,
                100L,
                "image-100-1234567890",
                ProductOutboxStatus.PENDING,
                retryCount,
                null,
                LocalDateTime.now(),
                null,
                50L,
                "https://example.com/image.jpg",
                "https://s3.example.com/image.jpg",
                ImageType.THUMBNAIL);
    }
}
