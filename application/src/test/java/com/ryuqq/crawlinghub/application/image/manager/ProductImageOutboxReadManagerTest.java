package com.ryuqq.crawlinghub.application.image.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
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
 * <p><strong>SRP</strong>: Outbox 조회만 테스트 (이미지 조회는 CrawledProductImageReadManagerTest)
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageOutboxReadManager 테스트")
class ProductImageOutboxReadManagerTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private ImageOutboxQueryPort outboxQueryPort;

    private ProductImageOutboxReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new ProductImageOutboxReadManager(outboxQueryPort);
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
}
