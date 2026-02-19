package com.ryuqq.crawlinghub.application.image.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;
import com.ryuqq.crawlinghub.application.image.manager.command.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * HandleImageUploadWebhookService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HandleImageUploadWebhookService 테스트")
class HandleImageUploadWebhookServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final Long OUTBOX_ID = 100L;
    private static final Long IMAGE_ID = 1L;
    private static final String EXTERNAL_DOWNLOAD_ID = "img-12345-abc";
    private static final String ORIGINAL_URL = "https://example.com/original.jpg";
    private static final String FILE_URL = "https://cdn.set-of.com/bucket/uploaded.jpg";
    private static final String FILE_ASSET_ID = "asset-uuid-123";
    private static final String ERROR_MESSAGE = "Upload failed: network error";

    @Mock private ProductImageOutboxReadManager outboxReadManager;

    @Mock private CrawledProductImageReadManager imageReadManager;

    @Mock private CrawledProductImageTransactionManager imageTransactionManager;

    @Mock private ProductImageOutboxTransactionManager outboxTransactionManager;

    @Mock private TimeProvider timeProvider;

    @Captor private ArgumentCaptor<CrawledProductImage> imageCaptor;

    private HandleImageUploadWebhookService service;

    @BeforeEach
    void setUp() {
        service =
                new HandleImageUploadWebhookService(
                        outboxReadManager,
                        imageReadManager,
                        imageTransactionManager,
                        outboxTransactionManager,
                        timeProvider);
        given(timeProvider.now()).willReturn(FIXED_INSTANT);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] COMPLETED 이벤트 → markAsCompleted 호출 + 이미지 S3 URL 업데이트")
        void shouldMarkAsCompletedWhenEventTypeIsCompleted() {
            // Given
            ImageUploadWebhookCommand command =
                    ImageUploadWebhookCommand.completed(
                            EXTERNAL_DOWNLOAD_ID, FILE_URL, FILE_ASSET_ID, FIXED_INSTANT);
            ProductImageOutbox outbox = createMockOutbox();
            CrawledProductImage image = createMockImage();

            given(outboxReadManager.findByIdempotencyKey(EXTERNAL_DOWNLOAD_ID))
                    .willReturn(Optional.of(outbox));
            given(imageReadManager.findById(IMAGE_ID)).willReturn(Optional.of(image));

            // When
            service.execute(command);

            // Then
            verify(outboxTransactionManager, times(1)).markAsCompleted(eq(outbox));
            // 비즈니스 로직(image.completeUpload) 호출 후 persist가 호출되는지 검증
            verify(imageTransactionManager, times(1)).persist(imageCaptor.capture());
            CrawledProductImage capturedImage = imageCaptor.getValue();
            org.assertj.core.api.Assertions.assertThat(capturedImage.getS3Url())
                    .isEqualTo(FILE_URL);
            org.assertj.core.api.Assertions.assertThat(capturedImage.getFileAssetId())
                    .isEqualTo(FILE_ASSET_ID);
            org.assertj.core.api.Assertions.assertThat(capturedImage.isUploaded()).isTrue();

            verify(outboxTransactionManager, never())
                    .markAsFailed(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("[성공] FAILED 이벤트 → markAsFailed 호출")
        void shouldMarkAsFailedWhenEventTypeIsFailed() {
            // Given
            ImageUploadWebhookCommand command =
                    ImageUploadWebhookCommand.failed(
                            EXTERNAL_DOWNLOAD_ID, ERROR_MESSAGE, FIXED_INSTANT);
            ProductImageOutbox outbox = createMockOutbox();

            given(outboxReadManager.findByIdempotencyKey(EXTERNAL_DOWNLOAD_ID))
                    .willReturn(Optional.of(outbox));

            // When
            service.execute(command);

            // Then
            verify(outboxTransactionManager, times(1)).markAsFailed(eq(outbox), eq(ERROR_MESSAGE));
            verify(outboxTransactionManager, never())
                    .markAsCompleted(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("[실패] Outbox 미존재 → IllegalArgumentException 발생")
        void shouldThrowExceptionWhenOutboxNotFound() {
            // Given
            ImageUploadWebhookCommand command =
                    ImageUploadWebhookCommand.completed(
                            EXTERNAL_DOWNLOAD_ID, FILE_URL, FILE_ASSET_ID, FIXED_INSTANT);

            given(outboxReadManager.findByIdempotencyKey(EXTERNAL_DOWNLOAD_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Outbox not found");
        }

        @Test
        @DisplayName("[성공] 알 수 없는 이벤트 타입 → 아무 작업 안 함")
        void shouldDoNothingWhenEventTypeIsUnknown() {
            // Given
            ImageUploadWebhookCommand command =
                    new ImageUploadWebhookCommand(
                            EXTERNAL_DOWNLOAD_ID, "UNKNOWN", null, null, null, FIXED_INSTANT);
            ProductImageOutbox outbox = createMockOutbox();

            given(outboxReadManager.findByIdempotencyKey(EXTERNAL_DOWNLOAD_ID))
                    .willReturn(Optional.of(outbox));

            // When
            service.execute(command);

            // Then
            verify(outboxTransactionManager, never())
                    .markAsCompleted(org.mockito.ArgumentMatchers.any());
            verify(outboxTransactionManager, never())
                    .markAsFailed(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.anyString());
        }
    }

    // === Helper Methods ===

    private ProductImageOutbox createMockOutbox() {
        return ProductImageOutbox.reconstitute(
                OUTBOX_ID,
                IMAGE_ID,
                EXTERNAL_DOWNLOAD_ID,
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }

    private CrawledProductImage createMockImage() {
        return CrawledProductImage.reconstitute(
                IMAGE_ID,
                PRODUCT_ID,
                ORIGINAL_URL,
                ImageType.THUMBNAIL,
                0,
                null,
                null,
                FIXED_INSTANT,
                null);
    }
}
