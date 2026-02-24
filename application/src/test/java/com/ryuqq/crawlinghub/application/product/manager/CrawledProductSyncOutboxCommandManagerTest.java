package com.ryuqq.crawlinghub.application.product.manager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductSyncOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductSyncOutboxCommandManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncOutboxCommandManager 단위 테스트")
class CrawledProductSyncOutboxCommandManagerTest {

    @Mock private CrawledProductSyncOutboxPersistencePort syncOutboxPersistencePort;

    @InjectMocks private CrawledProductSyncOutboxCommandManager sut;

    @Nested
    @DisplayName("persist() 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("[성공] Outbox 영속화 - PersistencePort에 위임")
        void shouldDelegatePersistToPersistencePort() {
            // Given
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();
            willDoNothing().given(syncOutboxPersistencePort).persist(outbox);

            // When
            sut.persist(outbox);

            // Then
            then(syncOutboxPersistencePort).should().persist(outbox);
        }
    }

    @Nested
    @DisplayName("markAsSent() 메서드 테스트")
    class MarkAsSentTest {

        @Test
        @DisplayName("[성공] SQS 발행 완료 → SENT 상태 전환 및 update 위임")
        void shouldMarkAsSentAndDelegateUpdate() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            willDoNothing().given(syncOutboxPersistencePort).update(any());

            // When
            sut.markAsSent(outbox);

            // Then
            then(syncOutboxPersistencePort).should().update(outbox);
        }
    }

    @Nested
    @DisplayName("markAsProcessing() 메서드 테스트")
    class MarkAsProcessingTest {

        @Test
        @DisplayName("[성공] 처리 시작 → PROCESSING 상태 전환 및 update 위임")
        void shouldMarkAsProcessingAndDelegateUpdate() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            willDoNothing().given(syncOutboxPersistencePort).update(any());

            // When
            sut.markAsProcessing(outbox);

            // Then
            then(syncOutboxPersistencePort).should().update(outbox);
        }
    }

    @Nested
    @DisplayName("markAsCompleted() 메서드 테스트")
    class MarkAsCompletedTest {

        @Test
        @DisplayName("[성공] 동기화 완료 → COMPLETED 상태 전환 및 update 위임")
        void shouldMarkAsCompletedAndDelegateUpdate() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedProcessing();
            Long externalProductId = 99999L;
            willDoNothing().given(syncOutboxPersistencePort).update(any());

            // When
            sut.markAsCompleted(outbox, externalProductId);

            // Then
            then(syncOutboxPersistencePort).should().update(outbox);
        }
    }

    @Nested
    @DisplayName("markAsFailed() 메서드 테스트")
    class MarkAsFailedTest {

        @Test
        @DisplayName("[성공] 처리 실패 → FAILED 상태 전환 및 update 위임")
        void shouldMarkAsFailedAndDelegateUpdate() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedProcessing();
            willDoNothing().given(syncOutboxPersistencePort).update(any());

            // When
            sut.markAsFailed(outbox, "API 호출 실패");

            // Then
            then(syncOutboxPersistencePort).should().update(outbox);
        }
    }

    @Nested
    @DisplayName("resetToPending() 메서드 테스트")
    class ResetToPendingTest {

        @Test
        @DisplayName("[성공] canRetry() 가능한 FAILED Outbox → PENDING으로 복구 및 update 위임")
        void shouldResetToPendingWhenCanRetry() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();
            willDoNothing().given(syncOutboxPersistencePort).update(any());

            // When
            sut.resetToPending(outbox);

            // Then
            then(syncOutboxPersistencePort).should().update(outbox);
        }

        @Test
        @DisplayName("[스킵] canRetry() 불가능한 Outbox → update 미호출")
        void shouldNotUpdateWhenCannotRetry() {
            // Given - 최대 재시도 횟수 초과한 Outbox (retryCount >= maxRetry)
            // CrawledProductSyncOutbox의 canRetry() 로직에 따라 초과 시 스킵
            // 이 테스트는 도메인 로직이 canRetry()=false를 반환하는 케이스를 커버
            // 실제로는 도메인 테스트에서 검증하는 부분이나, 매니저 레벨에서 update 미호출 확인
            CrawledProductSyncOutbox outbox =
                    com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox
                            .reconstitute(
                                    com.ryuqq.crawlinghub.domain.product.id
                                            .CrawledProductSyncOutboxId.of(1L),
                                    com.ryuqq.crawlinghub.domain.product.id.CrawledProductId.of(1L),
                                    com.ryuqq.crawlinghub.domain.seller.id.SellerId.of(100L),
                                    12345L,
                                    CrawledProductSyncOutbox.SyncType.CREATE,
                                    "sync-key-123",
                                    null,
                                    com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus
                                            .FAILED,
                                    Integer.MAX_VALUE, // 최대 재시도 초과
                                    "previous error",
                                    java.time.Instant.now(),
                                    java.time.Instant.now());

            // When
            sut.resetToPending(outbox);

            // Then
            then(syncOutboxPersistencePort).shouldHaveNoInteractions();
        }
    }
}
