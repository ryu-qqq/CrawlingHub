package com.ryuqq.crawlinghub.domain.product.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ProductImageOutbox 도메인 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("domain")
@DisplayName("ProductImageOutbox 단위 테스트")
class ProductImageOutboxTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final String ORIGINAL_URL = "https://example.com/image.jpg";

    @Nested
    @DisplayName("forNew() 테스트")
    class ForNewTests {

        @Test
        @DisplayName("성공 - CrawledProductImage로 신규 Outbox 생성")
        void shouldCreateNewOutboxFromImage() {
            // Given
            CrawledProductImage savedImage =
                    CrawledProductImage.reconstitute(
                            100L,
                            PRODUCT_ID,
                            ORIGINAL_URL,
                            ImageType.THUMBNAIL,
                            0,
                            null,
                            null,
                            FIXED_INSTANT,
                            null);

            // When
            ProductImageOutbox outbox = ProductImageOutbox.forNew(savedImage, FIXED_CLOCK);

            // Then
            assertThat(outbox.getId()).isNull();
            assertThat(outbox.getCrawledProductImageId()).isEqualTo(100L);
            assertThat(outbox.getIdempotencyKey()).hasSize(36); // UUID 형식
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isZero();
            assertThat(outbox.getErrorMessage()).isNull();
            assertThat(outbox.getCreatedAt()).isEqualTo(FIXED_INSTANT);
            assertThat(outbox.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("실패 - 저장되지 않은 이미지로 Outbox 생성 시 예외")
        void shouldThrowExceptionWhenImageNotSaved() {
            // Given
            CrawledProductImage unsavedImage =
                    CrawledProductImage.forNew(
                            PRODUCT_ID, ORIGINAL_URL, ImageType.THUMBNAIL, 0, FIXED_CLOCK);

            // When & Then
            assertThatThrownBy(() -> ProductImageOutbox.forNew(unsavedImage, FIXED_CLOCK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("먼저 저장");
        }
    }

    @Nested
    @DisplayName("forNewWithImageId() 테스트")
    class ForNewWithImageIdTests {

        @Test
        @DisplayName("성공 - 이미지 ID와 URL로 신규 Outbox 생성")
        void shouldCreateNewOutboxWithImageId() {
            // When
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);

            // Then
            assertThat(outbox.getId()).isNull();
            assertThat(outbox.getCrawledProductImageId()).isEqualTo(100L);
            assertThat(outbox.getIdempotencyKey()).hasSize(36); // UUID 형식
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
            assertThat(outbox.isPending()).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstitute() 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("성공 - 기존 데이터로 복원")
        void shouldReconstituteFromExistingData() {
            // When
            ProductImageOutbox outbox =
                    ProductImageOutbox.reconstitute(
                            1L,
                            100L,
                            "img-100-abc-12345678",
                            ProductOutboxStatus.PROCESSING,
                            1,
                            "previous error",
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // Then
            assertThat(outbox.getId()).isEqualTo(1L);
            assertThat(outbox.getCrawledProductImageId()).isEqualTo(100L);
            assertThat(outbox.getIdempotencyKey()).isEqualTo("img-100-abc-12345678");
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PROCESSING);
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getErrorMessage()).isEqualTo("previous error");
        }
    }

    @Nested
    @DisplayName("상태 변경 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("markAsProcessing - PENDING → PROCESSING")
        void shouldMarkAsProcessing() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            Clock laterClock = Clock.fixed(Instant.parse("2025-01-01T01:00:00Z"), ZoneId.of("UTC"));

            // When
            outbox.markAsProcessing(laterClock);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PROCESSING);
            assertThat(outbox.isPending()).isFalse();
            assertThat(outbox.getProcessedAt()).isEqualTo(Instant.parse("2025-01-01T01:00:00Z"));
        }

        @Test
        @DisplayName("markAsCompleted - PROCESSING → COMPLETED")
        void shouldMarkAsCompleted() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsProcessing(FIXED_CLOCK);
            Clock laterClock = Clock.fixed(Instant.parse("2025-01-01T02:00:00Z"), ZoneId.of("UTC"));

            // When
            outbox.markAsCompleted(laterClock);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.COMPLETED);
            assertThat(outbox.isCompleted()).isTrue();
            assertThat(outbox.getProcessedAt()).isEqualTo(Instant.parse("2025-01-01T02:00:00Z"));
        }

        @Test
        @DisplayName("markAsFailed - 에러 메시지 설정 및 retryCount 증가")
        void shouldMarkAsFailed() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsProcessing(FIXED_CLOCK);

            // When
            outbox.markAsFailed("Network timeout", FIXED_CLOCK);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
            assertThat(outbox.isFailed()).isTrue();
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getErrorMessage()).isEqualTo("Network timeout");
        }

        @Test
        @DisplayName("markAsFailed 여러 번 호출 시 retryCount 누적")
        void shouldIncrementRetryCountOnMultipleFailures() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);

            // When
            outbox.markAsFailed("Error 1", FIXED_CLOCK);
            outbox.markAsFailed("Error 2", FIXED_CLOCK);
            outbox.markAsFailed("Error 3", FIXED_CLOCK);

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(3);
            assertThat(outbox.getErrorMessage()).isEqualTo("Error 3");
        }
    }

    @Nested
    @DisplayName("resetToPending() 테스트")
    class ResetToPendingTests {

        @Test
        @DisplayName("성공 - 재시도 가능 시 PENDING으로 복귀")
        void shouldResetToPendingWhenCanRetry() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsFailed("Error", FIXED_CLOCK);

            // When
            outbox.resetToPending();

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
            assertThat(outbox.isPending()).isTrue();
            assertThat(outbox.getErrorMessage()).isNull();
            assertThat(outbox.getRetryCount()).isEqualTo(1); // retryCount는 유지
        }

        @Test
        @DisplayName("실패 - 재시도 불가능 시 상태 유지")
        void shouldNotResetWhenCannotRetry() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsFailed("Error 1", FIXED_CLOCK);
            outbox.markAsFailed("Error 2", FIXED_CLOCK);
            outbox.markAsFailed("Error 3", FIXED_CLOCK); // MAX_RETRY_COUNT = 3

            // When
            outbox.resetToPending();

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
            assertThat(outbox.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("canRetry() 테스트")
    class CanRetryTests {

        @Test
        @DisplayName("재시도 횟수 0 - 재시도 가능")
        void shouldAllowRetryWhenRetryCountIsZero() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);

            // Then
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("재시도 횟수 2 - 재시도 가능")
        void shouldAllowRetryWhenRetryCountIsLessThanMax() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsFailed("Error 1", FIXED_CLOCK);
            outbox.markAsFailed("Error 2", FIXED_CLOCK);

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(2);
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("재시도 횟수 3 (MAX) - 재시도 불가")
        void shouldNotAllowRetryWhenRetryCountReachesMax() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsFailed("Error 1", FIXED_CLOCK);
            outbox.markAsFailed("Error 2", FIXED_CLOCK);
            outbox.markAsFailed("Error 3", FIXED_CLOCK);

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(3);
            assertThat(outbox.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StatusCheckTests {

        @Test
        @DisplayName("isPending - PENDING 상태 확인")
        void shouldCheckPendingStatus() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);

            // Then
            assertThat(outbox.isPending()).isTrue();
            assertThat(outbox.isCompleted()).isFalse();
            assertThat(outbox.isFailed()).isFalse();
        }

        @Test
        @DisplayName("isCompleted - COMPLETED 상태 확인")
        void shouldCheckCompletedStatus() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsCompleted(FIXED_CLOCK);

            // Then
            assertThat(outbox.isPending()).isFalse();
            assertThat(outbox.isCompleted()).isTrue();
            assertThat(outbox.isFailed()).isFalse();
        }

        @Test
        @DisplayName("isFailed - FAILED 상태 확인")
        void shouldCheckFailedStatus() {
            // Given
            ProductImageOutbox outbox =
                    ProductImageOutbox.forNewWithImageId(100L, ORIGINAL_URL, FIXED_CLOCK);
            outbox.markAsFailed("Error", FIXED_CLOCK);

            // Then
            assertThat(outbox.isPending()).isFalse();
            assertThat(outbox.isCompleted()).isFalse();
            assertThat(outbox.isFailed()).isTrue();
        }
    }
}
