package com.ryuqq.crawlinghub.application.image.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;
import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
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
    private static final String IDEMPOTENCY_KEY = "idempotency-key-123";
    private static final String ORIGINAL_URL = "https://example.com/original.jpg";
    private static final String S3_URL = "https://s3.amazonaws.com/bucket/uploaded.jpg";
    private static final String ERROR_MESSAGE = "Upload failed: network error";

    @Mock private ImageOutboxReadManager imageOutboxReadManager;

    @Mock private ImageOutboxManager imageOutboxManager;

    private HandleImageUploadWebhookService service;

    @BeforeEach
    void setUp() {
        service = new HandleImageUploadWebhookService(imageOutboxReadManager, imageOutboxManager);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] COMPLETED 이벤트 → markAsCompleted 호출")
        void shouldMarkAsCompletedWhenEventTypeIsCompleted() {
            // Given
            ImageUploadWebhookCommand command =
                    ImageUploadWebhookCommand.completed(IDEMPOTENCY_KEY, S3_URL);
            CrawledProductImageOutbox outbox = createMockOutbox();

            given(imageOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));

            // When
            service.execute(command);

            // Then
            verify(imageOutboxManager, times(1)).markAsCompleted(eq(outbox), eq(S3_URL));
            verify(imageOutboxManager, never())
                    .markAsFailed(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("[성공] FAILED 이벤트 → markAsFailed 호출")
        void shouldMarkAsFailedWhenEventTypeIsFailed() {
            // Given
            ImageUploadWebhookCommand command =
                    ImageUploadWebhookCommand.failed(IDEMPOTENCY_KEY, ERROR_MESSAGE);
            CrawledProductImageOutbox outbox = createMockOutbox();

            given(imageOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));

            // When
            service.execute(command);

            // Then
            verify(imageOutboxManager, times(1)).markAsFailed(eq(outbox), eq(ERROR_MESSAGE));
            verify(imageOutboxManager, never())
                    .markAsCompleted(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("[실패] Outbox 미존재 → IllegalArgumentException 발생")
        void shouldThrowExceptionWhenOutboxNotFound() {
            // Given
            ImageUploadWebhookCommand command =
                    ImageUploadWebhookCommand.completed(IDEMPOTENCY_KEY, S3_URL);

            given(imageOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
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
                    new ImageUploadWebhookCommand(IDEMPOTENCY_KEY, "UNKNOWN", null, null);
            CrawledProductImageOutbox outbox = createMockOutbox();

            given(imageOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));

            // When
            service.execute(command);

            // Then
            verify(imageOutboxManager, never())
                    .markAsCompleted(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.anyString());
            verify(imageOutboxManager, never())
                    .markAsFailed(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.anyString());
        }
    }

    // === Helper Methods ===

    private CrawledProductImageOutbox createMockOutbox() {
        return CrawledProductImageOutbox.reconstitute(
                OUTBOX_ID,
                PRODUCT_ID,
                ORIGINAL_URL,
                ImageType.THUMBNAIL,
                IDEMPOTENCY_KEY,
                null,
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }
}
