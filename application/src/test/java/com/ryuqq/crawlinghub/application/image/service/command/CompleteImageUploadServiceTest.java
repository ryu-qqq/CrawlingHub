package com.ryuqq.crawlinghub.application.image.service.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadCompletedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
 * CompleteImageUploadService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteImageUploadService 테스트")
class CompleteImageUploadServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final Long OUTBOX_ID = 100L;
    private static final String ORIGINAL_URL = "https://example.com/original.jpg";
    private static final String S3_URL = "https://s3.amazonaws.com/bucket/uploaded.jpg";

    @Mock private ImageOutboxReadManager imageOutboxReadManager;

    @Mock private ImageOutboxManager imageOutboxManager;

    @Mock private TransactionEventRegistry eventRegistry;

    @Captor private ArgumentCaptor<ImageUploadCompletedEvent> eventCaptor;

    private CompleteImageUploadService service;

    @BeforeEach
    void setUp() {
        service =
                new CompleteImageUploadService(
                        imageOutboxReadManager, imageOutboxManager, eventRegistry, FIXED_CLOCK);
    }

    @Nested
    @DisplayName("complete() 테스트")
    class Complete {

        @Test
        @DisplayName("[성공] Outbox 존재 → markAsCompleted + 이벤트 등록")
        void shouldMarkAsCompletedAndRegisterEventWhenOutboxExists() {
            // Given
            CrawledProductImageOutbox outbox = createMockOutbox();

            given(imageOutboxReadManager.findById(OUTBOX_ID)).willReturn(Optional.of(outbox));

            // When
            service.complete(OUTBOX_ID, S3_URL);

            // Then
            verify(imageOutboxManager, times(1)).markAsCompleted(eq(outbox), eq(S3_URL));
            verify(eventRegistry, times(1)).registerForPublish(eventCaptor.capture());

            ImageUploadCompletedEvent event = eventCaptor.getValue();
            org.assertj.core.api.Assertions.assertThat(event.crawledProductId())
                    .isEqualTo(PRODUCT_ID);
            org.assertj.core.api.Assertions.assertThat(event.originalUrl()).isEqualTo(ORIGINAL_URL);
            org.assertj.core.api.Assertions.assertThat(event.s3Url()).isEqualTo(S3_URL);
        }

        @Test
        @DisplayName("[성공] Outbox 미존재 → 아무 작업 안 함")
        void shouldDoNothingWhenOutboxNotExists() {
            // Given
            given(imageOutboxReadManager.findById(OUTBOX_ID)).willReturn(Optional.empty());

            // When
            service.complete(OUTBOX_ID, S3_URL);

            // Then
            verify(imageOutboxManager, never()).markAsCompleted(any(), any());
            verify(eventRegistry, never()).registerForPublish(any());
        }
    }

    // === Helper Methods ===

    private CrawledProductImageOutbox createMockOutbox() {
        return CrawledProductImageOutbox.reconstitute(
                OUTBOX_ID,
                PRODUCT_ID,
                ORIGINAL_URL,
                ImageType.THUMBNAIL,
                "idempotency-key-123",
                null,
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }
}
