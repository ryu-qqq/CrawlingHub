package com.ryuqq.crawlinghub.application.image.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.image.dto.response.ImageUploadRetryResponse;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RetryImageUploadService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetryImageUploadService 테스트")
class RetryImageUploadServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);

    @Mock private ProductImageOutboxReadManager outboxReadManager;

    @Mock private CrawledProductImageReadManager imageReadManager;

    @Mock private ProductImageOutboxTransactionManager outboxTransactionManager;

    @Mock private FileServerClient fileServerClient;

    private RetryImageUploadService service;

    @BeforeEach
    void setUp() {
        service =
                new RetryImageUploadService(
                        outboxReadManager,
                        imageReadManager,
                        outboxTransactionManager,
                        fileServerClient);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 재시도 대상 없음 → empty 결과 반환")
        void shouldReturnEmptyWhenNoRetryableOutboxes() {
            // Given
            given(outboxReadManager.findRetryableOutboxes(anyInt(), anyInt()))
                    .willReturn(List.of());

            // When
            ImageUploadRetryResponse result = service.execute();

            // Then
            assertThat(result.processed()).isZero();
            assertThat(result.succeeded()).isZero();
            assertThat(result.failed()).isZero();
            assertThat(result.hasMore()).isFalse();

            verify(fileServerClient, never()).requestImageUpload(any());
        }

        @Test
        @DisplayName("[성공] 재시도 성공 → PROCESSING 상태로 변경")
        void shouldMarkAsProcessingWhenRetrySucceeds() {
            // Given
            ProductImageOutbox outbox = createMockOutbox(1L, 1L, "key-1");
            CrawledProductImage image = createMockImage(1L);

            given(outboxReadManager.findRetryableOutboxes(anyInt(), anyInt()))
                    .willReturn(List.of(outbox));
            given(imageReadManager.findById(1L)).willReturn(Optional.of(image));
            given(fileServerClient.requestImageUpload(any())).willReturn(true);

            // When
            ImageUploadRetryResponse result = service.execute();

            // Then
            assertThat(result.processed()).isEqualTo(1);
            assertThat(result.succeeded()).isEqualTo(1);
            assertThat(result.failed()).isZero();

            verify(outboxTransactionManager, times(1)).markAsProcessing(eq(outbox));
            verify(outboxTransactionManager, never()).markAsFailed(any(), anyString());
        }

        @Test
        @DisplayName("[성공] 재시도 실패 → FAILED 상태로 변경")
        void shouldMarkAsFailedWhenRetryFails() {
            // Given
            ProductImageOutbox outbox = createMockOutbox(1L, 1L, "key-1");
            CrawledProductImage image = createMockImage(1L);

            given(outboxReadManager.findRetryableOutboxes(anyInt(), anyInt()))
                    .willReturn(List.of(outbox));
            given(imageReadManager.findById(1L)).willReturn(Optional.of(image));
            given(fileServerClient.requestImageUpload(any())).willReturn(false);

            // When
            ImageUploadRetryResponse result = service.execute();

            // Then
            assertThat(result.processed()).isEqualTo(1);
            assertThat(result.succeeded()).isZero();
            assertThat(result.failed()).isEqualTo(1);

            verify(outboxTransactionManager, times(1)).markAsFailed(eq(outbox), anyString());
            verify(outboxTransactionManager, never()).markAsProcessing(any());
        }

        @Test
        @DisplayName("[성공] 복수 Outbox 처리 → 성공/실패 카운트 정확")
        void shouldCountSuccessAndFailureCorrectly() {
            // Given
            ProductImageOutbox outbox1 = createMockOutbox(1L, 1L, "key-1");
            ProductImageOutbox outbox2 = createMockOutbox(2L, 2L, "key-2");
            ProductImageOutbox outbox3 = createMockOutbox(3L, 3L, "key-3");

            CrawledProductImage image1 = createMockImage(1L);
            CrawledProductImage image2 = createMockImage(2L);
            CrawledProductImage image3 = createMockImage(3L);

            given(outboxReadManager.findRetryableOutboxes(anyInt(), anyInt()))
                    .willReturn(List.of(outbox1, outbox2, outbox3));
            given(imageReadManager.findById(1L)).willReturn(Optional.of(image1));
            given(imageReadManager.findById(2L)).willReturn(Optional.of(image2));
            given(imageReadManager.findById(3L)).willReturn(Optional.of(image3));
            given(fileServerClient.requestImageUpload(any()))
                    .willReturn(true) // outbox1 성공
                    .willReturn(false) // outbox2 실패
                    .willReturn(true); // outbox3 성공

            // When
            ImageUploadRetryResponse result = service.execute();

            // Then
            assertThat(result.processed()).isEqualTo(3);
            assertThat(result.succeeded()).isEqualTo(2);
            assertThat(result.failed()).isEqualTo(1);
        }

        @Test
        @DisplayName("[성공] 배치 크기만큼 조회 시 → hasMore = true")
        void shouldSetHasMoreTrueWhenBatchSizeReached() {
            // Given: 100개 (BATCH_SIZE)만큼 반환
            List<ProductImageOutbox> outboxes =
                    java.util.stream.IntStream.rangeClosed(1, 100)
                            .mapToObj(i -> createMockOutbox((long) i, (long) i, "key-" + i))
                            .toList();

            given(outboxReadManager.findRetryableOutboxes(anyInt(), anyInt())).willReturn(outboxes);

            // 모든 이미지에 대한 mock 설정
            for (int i = 1; i <= 100; i++) {
                given(imageReadManager.findById((long) i))
                        .willReturn(Optional.of(createMockImage((long) i)));
            }

            given(fileServerClient.requestImageUpload(any())).willReturn(true);

            // When
            ImageUploadRetryResponse result = service.execute();

            // Then
            assertThat(result.processed()).isEqualTo(100);
            assertThat(result.hasMore()).isTrue();
        }
    }

    // === Helper Methods ===

    private ProductImageOutbox createMockOutbox(Long id, Long imageId, String idempotencyKey) {
        return ProductImageOutbox.reconstitute(
                id,
                imageId,
                idempotencyKey,
                ProductOutboxStatus.FAILED,
                1,
                "Previous error",
                FIXED_INSTANT,
                FIXED_INSTANT);
    }

    private CrawledProductImage createMockImage(Long id) {
        return CrawledProductImage.reconstitute(
                id,
                PRODUCT_ID,
                "https://example.com/original-" + id + ".jpg",
                ImageType.THUMBNAIL,
                0,
                null,
                null,
                FIXED_INSTANT,
                null);
    }
}
