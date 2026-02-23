package com.ryuqq.crawlinghub.application.product.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductSyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductSyncOutboxReadManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: QueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncOutboxReadManager 단위 테스트")
class CrawledProductSyncOutboxReadManagerTest {

    @Mock private CrawledProductSyncOutboxQueryPort syncOutboxQueryPort;
    @Mock private CrawledProductSyncOutbox outbox;

    private CrawledProductSyncOutboxReadManager sut;

    @BeforeEach
    void setUp() {
        sut = new CrawledProductSyncOutboxReadManager(syncOutboxQueryPort);
    }

    @Nested
    @DisplayName("findById() 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("[성공] ID로 Outbox 조회 위임")
        void shouldDelegateToQueryPort() {
            // Given
            given(syncOutboxQueryPort.findById(1L)).willReturn(Optional.of(outbox));

            // When
            Optional<CrawledProductSyncOutbox> result = sut.findById(1L);

            // Then
            assertThat(result).isPresent().contains(outbox);
            verify(syncOutboxQueryPort).findById(1L);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            given(syncOutboxQueryPort.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<CrawledProductSyncOutbox> result = sut.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdempotencyKey() 메서드 테스트")
    class FindByIdempotencyKeyTest {

        @Test
        @DisplayName("[성공] 멱등성 키로 Outbox 조회 위임")
        void shouldDelegateToQueryPort() {
            // Given
            String idempotencyKey = "sync-key-123";
            given(syncOutboxQueryPort.findByIdempotencyKey(idempotencyKey))
                    .willReturn(Optional.of(outbox));

            // When
            Optional<CrawledProductSyncOutbox> result = sut.findByIdempotencyKey(idempotencyKey);

            // Then
            assertThat(result).isPresent().contains(outbox);
            verify(syncOutboxQueryPort).findByIdempotencyKey(idempotencyKey);
        }
    }

    @Nested
    @DisplayName("findRetryableOutboxes() 메서드 테스트")
    class FindRetryableOutboxesTest {

        @Test
        @DisplayName("[성공] 재시도 가능한 Outbox 목록 조회 위임")
        void shouldDelegateToQueryPort() {
            // Given
            int maxRetryCount = 3;
            int limit = 100;
            CrawledProductSyncOutbox pendingOutbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            given(syncOutboxQueryPort.findRetryableOutboxes(maxRetryCount, limit))
                    .willReturn(List.of(pendingOutbox));

            // When
            List<CrawledProductSyncOutbox> result = sut.findRetryableOutboxes(maxRetryCount, limit);

            // Then
            assertThat(result).hasSize(1).contains(pendingOutbox);
            verify(syncOutboxQueryPort).findRetryableOutboxes(maxRetryCount, limit);
        }

        @Test
        @DisplayName("[성공] 결과 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoRetryableOutboxes() {
            // Given
            given(syncOutboxQueryPort.findRetryableOutboxes(3, 100)).willReturn(List.of());

            // When
            List<CrawledProductSyncOutbox> result = sut.findRetryableOutboxes(3, 100);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findFailedOlderThan() 메서드 테스트")
    class FindFailedOlderThanTest {

        @Test
        @DisplayName("[성공] FAILED 이후 일정 시간 경과한 Outbox 조회 위임")
        void shouldDelegateToQueryPort() {
            // Given
            int limit = 100;
            int delaySeconds = 300;
            CrawledProductSyncOutbox failedOutbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();
            given(syncOutboxQueryPort.findFailedOlderThan(limit, delaySeconds))
                    .willReturn(List.of(failedOutbox));

            // When
            List<CrawledProductSyncOutbox> result = sut.findFailedOlderThan(limit, delaySeconds);

            // Then
            assertThat(result).hasSize(1).contains(failedOutbox);
            verify(syncOutboxQueryPort).findFailedOlderThan(limit, delaySeconds);
        }
    }

    @Nested
    @DisplayName("findStaleProcessing() 메서드 테스트")
    class FindStaleProcessingTest {

        @Test
        @DisplayName("[성공] 타임아웃된 PROCESSING Outbox 조회 위임")
        void shouldDelegateToQueryPort() {
            // Given
            int limit = 50;
            long timeoutSeconds = 600L;
            CrawledProductSyncOutbox processingOutbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedProcessing();
            given(syncOutboxQueryPort.findStaleProcessing(limit, timeoutSeconds))
                    .willReturn(List.of(processingOutbox));

            // When
            List<CrawledProductSyncOutbox> result = sut.findStaleProcessing(limit, timeoutSeconds);

            // Then
            assertThat(result).hasSize(1).contains(processingOutbox);
            verify(syncOutboxQueryPort).findStaleProcessing(limit, timeoutSeconds);
        }
    }

    @Nested
    @DisplayName("existsActiveOutbox() 메서드 테스트")
    class ExistsActiveOutboxTest {

        @Test
        @DisplayName("[성공] 활성 Outbox 존재 시 true 반환")
        void shouldReturnTrueWhenActiveOutboxExists() {
            // Given
            CrawledProductId productId = CrawledProductId.of(1L);
            SyncType syncType = SyncType.CREATE;
            given(
                            syncOutboxQueryPort.existsByProductIdAndSyncTypeAndStatuses(
                                    productId.value(),
                                    syncType,
                                    List.of(
                                            ProductOutboxStatus.PENDING,
                                            ProductOutboxStatus.SENT,
                                            ProductOutboxStatus.PROCESSING)))
                    .willReturn(true);

            // When
            boolean result = sut.existsActiveOutbox(productId, syncType);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[성공] 활성 Outbox 미존재 시 false 반환")
        void shouldReturnFalseWhenNoActiveOutbox() {
            // Given
            CrawledProductId productId = CrawledProductId.of(2L);
            SyncType syncType = SyncType.UPDATE_PRICE;
            given(
                            syncOutboxQueryPort.existsByProductIdAndSyncTypeAndStatuses(
                                    productId.value(),
                                    syncType,
                                    List.of(
                                            ProductOutboxStatus.PENDING,
                                            ProductOutboxStatus.SENT,
                                            ProductOutboxStatus.PROCESSING)))
                    .willReturn(false);

            // When
            boolean result = sut.existsActiveOutbox(productId, syncType);

            // Then
            assertThat(result).isFalse();
        }
    }
}
