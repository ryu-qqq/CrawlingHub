package com.ryuqq.crawlinghub.application.image.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.command.ImageOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProductImageOutboxTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductImageOutboxTransactionManager 테스트")
class ProductImageOutboxTransactionManagerTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private ImageOutboxPersistencePort outboxPersistencePort;

    @Mock private TimeProvider timeProvider;

    private ProductImageOutboxTransactionManager manager;

    @BeforeEach
    void setUp() {
        manager = new ProductImageOutboxTransactionManager(outboxPersistencePort, timeProvider);
        given(timeProvider.now()).willReturn(FIXED_TIME);
    }

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] Outbox 단건 저장")
        void shouldPersistOutbox() {
            // Given
            ProductImageOutbox outbox = createPendingOutbox(1L);

            // When
            manager.persist(outbox);

            // Then
            verify(outboxPersistencePort).persist(outbox);
        }
    }

    @Nested
    @DisplayName("persistAll() 테스트")
    class PersistAll {

        @Test
        @DisplayName("[성공] Outbox 벌크 저장")
        void shouldPersistAllOutboxes() {
            // Given
            List<ProductImageOutbox> outboxes =
                    List.of(createPendingOutbox(1L), createPendingOutbox(2L));

            // When
            manager.persistAll(outboxes);

            // Then
            verify(outboxPersistencePort).persistAll(outboxes);
        }

        @Test
        @DisplayName("[성공] 빈 목록 입력 → 저장하지 않음")
        void shouldNotPersistEmptyList() {
            // Given
            List<ProductImageOutbox> emptyList = List.of();

            // When
            manager.persistAll(emptyList);

            // Then
            verifyNoInteractions(outboxPersistencePort);
        }

        @Test
        @DisplayName("[성공] null 입력 → 저장하지 않음")
        void shouldNotPersistNullList() {
            // When
            manager.persistAll(null);

            // Then
            verifyNoInteractions(outboxPersistencePort);
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StateTransition {

        @Test
        @DisplayName("[성공] PROCESSING 상태로 변경")
        void shouldMarkAsProcessing() {
            // Given
            ProductImageOutbox outbox = createPendingOutbox(1L);

            // When
            manager.markAsProcessing(outbox);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PROCESSING);
            verify(outboxPersistencePort).update(outbox);
        }

        @Test
        @DisplayName("[성공] COMPLETED 상태로 변경")
        void shouldMarkAsCompleted() {
            // Given
            ProductImageOutbox outbox = createProcessingOutbox(1L);

            // When
            manager.markAsCompleted(outbox);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.COMPLETED);
            verify(outboxPersistencePort).update(outbox);
        }

        @Test
        @DisplayName("[성공] FAILED 상태로 변경")
        void shouldMarkAsFailed() {
            // Given
            ProductImageOutbox outbox = createProcessingOutbox(1L);
            String errorMessage = "업로드 실패";

            // When
            manager.markAsFailed(outbox, errorMessage);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
            assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
            verify(outboxPersistencePort).update(outbox);
        }

        @Test
        @DisplayName("[성공] 재시도 가능한 경우 PENDING으로 복귀")
        void shouldResetToPendingWhenRetryable() {
            // Given
            ProductImageOutbox outbox = createFailedOutbox(1L, 1);

            // When
            manager.resetToPending(outbox);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
            verify(outboxPersistencePort).update(outbox);
        }

        @Test
        @DisplayName("[성공] 재시도 불가능한 경우 상태 변경 없음")
        void shouldNotResetWhenNotRetryable() {
            // Given
            ProductImageOutbox outbox = createFailedOutbox(1L, 3); // MAX_RETRY_COUNT = 3

            // When
            manager.resetToPending(outbox);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
            verifyNoInteractions(outboxPersistencePort);
        }
    }

    // === Helper Methods ===

    private ProductImageOutbox createPendingOutbox(Long id) {
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

    private ProductImageOutbox createProcessingOutbox(Long id) {
        return ProductImageOutbox.reconstitute(
                id,
                100L,
                "test-idempotency-key-" + id,
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                FIXED_TIME,
                FIXED_TIME);
    }

    private ProductImageOutbox createFailedOutbox(Long id, int retryCount) {
        return ProductImageOutbox.reconstitute(
                id,
                100L,
                "test-idempotency-key-" + id,
                ProductOutboxStatus.FAILED,
                retryCount,
                "Previous error",
                FIXED_TIME,
                FIXED_TIME);
    }
}
