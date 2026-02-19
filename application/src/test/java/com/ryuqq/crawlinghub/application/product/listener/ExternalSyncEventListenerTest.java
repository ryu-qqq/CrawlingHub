package com.ryuqq.crawlinghub.application.product.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.sync.manager.messaging.ProductSyncMessageManager;
import com.ryuqq.crawlinghub.application.sync.manager.query.SyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ExternalSyncEventListener 단위 테스트
 *
 * <p>SQS 발행 기반 이벤트 리스너 테스트
 *
 * <p>Transactional Outbox 패턴:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>SyncOutbox 조회
 *   <li>SQS로 메시지 발행 시도
 *   <li>성공 시: SENT 상태로 전환
 *   <li>실패 시: PENDING 유지 → 스케줄러에서 재처리
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalSyncEventListener 테스트")
class ExternalSyncEventListenerTest {

    @Mock private SyncOutboxReadManager syncOutboxReadManager;

    @Mock private SyncOutboxTransactionManager syncOutboxTransactionManager;

    @Mock private ProductSyncMessageManager messageManager;

    @InjectMocks private ExternalSyncEventListener listener;

    private static final Instant FIXED_INSTANT = Instant.parse("2025-11-27T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;
    private static final String IDEMPOTENCY_KEY = "sync-1-create-abc12345";

    @Nested
    @DisplayName("handleExternalSyncRequested() 테스트")
    class HandleExternalSyncRequested {

        @Test
        @DisplayName("[성공] CREATE 요청 → SQS 발행 → SENT 상태로 변경")
        void shouldPublishToSqsAndMarkAsSentForCreateRequest() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.CREATE);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            willDoNothing().given(messageManager).publish(outbox);

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(messageManager).should().publish(outbox);
            then(syncOutboxTransactionManager).should().markAsSent(outbox);
        }

        @Test
        @DisplayName("[성공] UPDATE 요청 → SQS 발행 → SENT 상태로 변경")
        void shouldPublishToSqsAndMarkAsSentForUpdateRequest() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.UPDATE);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            willDoNothing().given(messageManager).publish(outbox);

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(messageManager).should().publish(outbox);
            then(syncOutboxTransactionManager).should().markAsSent(outbox);
        }

        @Test
        @DisplayName("[실패] Outbox 미존재 → 아무 작업 안함")
        void shouldDoNothingWhenOutboxNotFound() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.CREATE);

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.empty());

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(messageManager).shouldHaveNoInteractions();
            then(syncOutboxTransactionManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[실패] SQS 발행 실패 → PENDING 유지 (스케줄러에서 재처리)")
        void shouldKeepPendingWhenSqsPublishFails() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.CREATE);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            willThrow(new RuntimeException("SQS connection failed"))
                    .given(messageManager)
                    .publish(outbox);

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(messageManager).should().publish(outbox);
            then(syncOutboxTransactionManager).should(never()).markAsSent(any());
        }
    }

    // === Helper Methods ===

    private ExternalSyncRequestedEvent createEvent(ExternalSyncRequestedEvent.SyncType syncType) {
        return new ExternalSyncRequestedEvent(
                PRODUCT_ID, SELLER_ID, ITEM_NO, IDEMPOTENCY_KEY, syncType, FIXED_INSTANT);
    }
}
