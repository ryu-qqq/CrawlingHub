package com.ryuqq.crawlinghub.application.image.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductImageQueryPort;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
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
 * ImageOutboxReadManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: QueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageOutboxReadManager 테스트")
class ImageOutboxReadManagerTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private ImageOutboxQueryPort outboxQueryPort;
    @Mock private CrawledProductImageQueryPort imageQueryPort;

    private ImageOutboxReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new ImageOutboxReadManager(outboxQueryPort, imageQueryPort);
    }

    @Nested
    @DisplayName("Outbox 조회 테스트")
    class OutboxQuery {

        @Test
        @DisplayName("[성공] ID로 Outbox 조회")
        void shouldFindById() {
            // Given
            Long outboxId = 1L;
            ProductImageOutbox outbox = createOutbox(outboxId);
            given(outboxQueryPort.findById(outboxId)).willReturn(Optional.of(outbox));

            // When
            Optional<ProductImageOutbox> result = manager.findById(outboxId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(outboxId);
            verify(outboxQueryPort).findById(outboxId);
        }

        @Test
        @DisplayName("[성공] Idempotency Key로 Outbox 조회")
        void shouldFindByIdempotencyKey() {
            // Given
            String idempotencyKey = "test-idempotency-key";
            ProductImageOutbox outbox = createOutbox(1L);
            given(outboxQueryPort.findByIdempotencyKey(idempotencyKey))
                    .willReturn(Optional.of(outbox));

            // When
            Optional<ProductImageOutbox> result = manager.findByIdempotencyKey(idempotencyKey);

            // Then
            assertThat(result).isPresent();
            verify(outboxQueryPort).findByIdempotencyKey(idempotencyKey);
        }

        @Test
        @DisplayName("[성공] 이미지 ID로 Outbox 조회")
        void shouldFindByCrawledProductImageId() {
            // Given
            Long imageId = 100L;
            ProductImageOutbox outbox = createOutbox(1L);
            given(outboxQueryPort.findByCrawledProductImageId(imageId))
                    .willReturn(Optional.of(outbox));

            // When
            Optional<ProductImageOutbox> result = manager.findByCrawledProductImageId(imageId);

            // Then
            assertThat(result).isPresent();
            verify(outboxQueryPort).findByCrawledProductImageId(imageId);
        }

        @Test
        @DisplayName("[성공] 상태로 Outbox 목록 조회")
        void shouldFindByStatus() {
            // Given
            ProductOutboxStatus status = ProductOutboxStatus.PENDING;
            int limit = 10;
            List<ProductImageOutbox> outboxes = List.of(createOutbox(1L), createOutbox(2L));
            given(outboxQueryPort.findByStatus(status, limit)).willReturn(outboxes);

            // When
            List<ProductImageOutbox> result = manager.findByStatus(status, limit);

            // Then
            assertThat(result).hasSize(2);
            verify(outboxQueryPort).findByStatus(status, limit);
        }

        @Test
        @DisplayName("[성공] PENDING 상태 Outbox 조회")
        void shouldFindPendingOutboxes() {
            // Given
            int limit = 10;
            List<ProductImageOutbox> outboxes = List.of(createOutbox(1L), createOutbox(2L));
            given(outboxQueryPort.findPendingOutboxes(limit)).willReturn(outboxes);

            // When
            List<ProductImageOutbox> result = manager.findPendingOutboxes(limit);

            // Then
            assertThat(result).hasSize(2);
            verify(outboxQueryPort).findPendingOutboxes(limit);
        }

        @Test
        @DisplayName("[성공] 재시도 가능한 Outbox 조회")
        void shouldFindRetryableOutboxes() {
            // Given
            int maxRetryCount = 3;
            int limit = 10;
            List<ProductImageOutbox> outboxes = List.of(createOutbox(1L));
            given(outboxQueryPort.findRetryableOutboxes(maxRetryCount, limit)).willReturn(outboxes);

            // When
            List<ProductImageOutbox> result = manager.findRetryableOutboxes(maxRetryCount, limit);

            // Then
            assertThat(result).hasSize(1);
            verify(outboxQueryPort).findRetryableOutboxes(maxRetryCount, limit);
        }
    }

    @Nested
    @DisplayName("이미지 조회 테스트")
    class ImageQuery {

        @Test
        @DisplayName("[성공] ID로 이미지 조회")
        void shouldFindImageById() {
            // Given
            Long imageId = 1L;
            CrawledProductImage image = createImage(imageId, 100L);
            given(imageQueryPort.findById(imageId)).willReturn(Optional.of(image));

            // When
            Optional<CrawledProductImage> result = manager.findImageById(imageId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(imageId);
            verify(imageQueryPort).findById(imageId);
        }

        @Test
        @DisplayName("[성공] CrawledProductId로 이미지 목록 조회")
        void shouldFindImagesByCrawledProductId() {
            // Given
            CrawledProductId crawledProductId = CrawledProductId.of(100L);
            List<CrawledProductImage> images =
                    List.of(createImage(1L, 100L), createImage(2L, 100L));
            given(imageQueryPort.findByCrawledProductId(crawledProductId)).willReturn(images);

            // When
            List<CrawledProductImage> result =
                    manager.findImagesByCrawledProductId(crawledProductId);

            // Then
            assertThat(result).hasSize(2);
            verify(imageQueryPort).findByCrawledProductId(crawledProductId);
        }

        @Test
        @DisplayName("[성공] CrawledProductId와 원본 URL로 이미지 조회")
        void shouldFindImageByCrawledProductIdAndOriginalUrl() {
            // Given
            CrawledProductId crawledProductId = CrawledProductId.of(100L);
            String originalUrl = "https://example.com/image.jpg";
            CrawledProductImage image = createImage(1L, 100L);
            given(
                            imageQueryPort.findByCrawledProductIdAndOriginalUrl(
                                    crawledProductId, originalUrl))
                    .willReturn(Optional.of(image));

            // When
            Optional<CrawledProductImage> result =
                    manager.findImageByCrawledProductIdAndOriginalUrl(
                            crawledProductId, originalUrl);

            // Then
            assertThat(result).isPresent();
            verify(imageQueryPort)
                    .findByCrawledProductIdAndOriginalUrl(crawledProductId, originalUrl);
        }
    }

    @Nested
    @DisplayName("filterNewImageUrls() 테스트")
    class FilterNewImageUrls {

        @Test
        @DisplayName("[성공] 새로운 이미지 URL만 필터링")
        void shouldFilterNewImageUrls() {
            // Given
            CrawledProductId crawledProductId = CrawledProductId.of(100L);
            List<String> imageUrls =
                    List.of(
                            "https://example.com/new1.jpg",
                            "https://example.com/existing.jpg",
                            "https://example.com/new2.jpg");
            List<String> existingUrls = List.of("https://example.com/existing.jpg");
            given(imageQueryPort.findExistingOriginalUrls(crawledProductId, imageUrls))
                    .willReturn(existingUrls);

            // When
            List<String> result = manager.filterNewImageUrls(crawledProductId, imageUrls);

            // Then
            assertThat(result)
                    .hasSize(2)
                    .containsExactlyInAnyOrder(
                            "https://example.com/new1.jpg", "https://example.com/new2.jpg");
        }

        @Test
        @DisplayName("[성공] 빈 목록 입력 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyInput() {
            // Given
            CrawledProductId crawledProductId = CrawledProductId.of(100L);
            List<String> emptyList = List.of();

            // When
            List<String> result = manager.filterNewImageUrls(crawledProductId, emptyList);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] null 입력 → 빈 목록 반환")
        void shouldReturnEmptyListForNullInput() {
            // Given
            CrawledProductId crawledProductId = CrawledProductId.of(100L);

            // When
            List<String> result = manager.filterNewImageUrls(crawledProductId, null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // === Helper Methods ===

    private ProductImageOutbox createOutbox(Long id) {
        return ProductImageOutbox.reconstitute(
                id,
                100L,
                "test-idempotency-key-" + id,
                ProductOutboxStatus.PENDING,
                0,
                null,
                FIXED_TIME,
                null);
    }

    private CrawledProductImage createImage(Long id, Long crawledProductId) {
        return CrawledProductImage.reconstitute(
                id,
                CrawledProductId.of(crawledProductId),
                "https://example.com/image" + id + ".jpg",
                ImageType.THUMBNAIL,
                1,
                null,
                null,
                FIXED_TIME,
                null);
    }
}
