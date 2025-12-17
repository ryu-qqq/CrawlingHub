package com.ryuqq.crawlinghub.application.product.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.facade.SyncCompletionFacade;
import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.application.sync.manager.SyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
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
 * <p>Mockist 스타일 테스트: Manager, Facade, Port, Client Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalSyncEventListener 테스트")
class ExternalSyncEventListenerTest {

    @Mock private SyncOutboxReadManager syncOutboxReadManager;

    @Mock private SyncOutboxManager syncOutboxManager;

    @Mock private CrawledProductReadManager crawledProductReadManager;

    @Mock private SyncCompletionFacade syncCompletionFacade;

    @Mock private ExternalProductServerClient externalProductServerClient;

    @InjectMocks private ExternalSyncEventListener listener;

    private static final Clock FIXED_CLOCK = FixedClock.aDefaultClock();
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;
    private static final String IDEMPOTENCY_KEY = "sync-1-create-abc12345";
    private static final Long EXTERNAL_PRODUCT_ID = 99999L;

    @Nested
    @DisplayName("handleExternalSyncRequested() 테스트")
    class HandleExternalSyncRequested {

        @Test
        @DisplayName("[성공] CREATE 요청 → 외부 API 호출 → COMPLETED 상태로 변경")
        void shouldCallCreateApiAndMarkAsCompletedForCreateRequest() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.CREATE);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            CrawledProduct product = createSyncReadyProduct(null);

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(externalProductServerClient.createProduct(any(), any()))
                    .willReturn(ProductSyncResult.success(EXTERNAL_PRODUCT_ID));

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(crawledProductReadManager).should().findById(PRODUCT_ID);
            then(syncOutboxManager).should().markAsProcessing(outbox);
            then(externalProductServerClient).should().createProduct(any(), any());
            then(syncCompletionFacade).should().completeSync(outbox, product, EXTERNAL_PRODUCT_ID);
        }

        @Test
        @DisplayName("[성공] UPDATE 요청 → 외부 API 호출 → COMPLETED 상태로 변경")
        void shouldCallUpdateApiAndMarkAsCompletedForUpdateRequest() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.UPDATE);
            CrawledProductSyncOutbox outbox = createUpdateOutbox();
            CrawledProduct product = createSyncReadyProduct(EXTERNAL_PRODUCT_ID);

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(externalProductServerClient.updateProduct(any(), any()))
                    .willReturn(ProductSyncResult.success(EXTERNAL_PRODUCT_ID));

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(crawledProductReadManager).should().findById(PRODUCT_ID);
            then(syncOutboxManager).should().markAsProcessing(outbox);
            then(externalProductServerClient).should().updateProduct(any(), any());
            then(syncCompletionFacade).should().completeSync(outbox, product, EXTERNAL_PRODUCT_ID);
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
            then(crawledProductReadManager).shouldHaveNoInteractions();
            then(syncOutboxManager).shouldHaveNoInteractions();
            then(externalProductServerClient).shouldHaveNoInteractions();
            then(syncCompletionFacade).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[실패] CrawledProduct 미존재 → FAILED 상태로 변경")
        void shouldMarkAsFailedWhenProductNotFound() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.CREATE);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.empty());

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(crawledProductReadManager).should().findById(PRODUCT_ID);
            then(syncCompletionFacade).should().failSync(eq(outbox), anyString());
            then(syncOutboxManager).should(never()).markAsProcessing(any());
            then(externalProductServerClient).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[실패] 외부 API 호출 실패 → FAILED 상태로 변경")
        void shouldMarkAsFailedWhenApiCallFails() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.CREATE);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            CrawledProduct product = createSyncReadyProduct(null);

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(externalProductServerClient.createProduct(any(), any()))
                    .willReturn(ProductSyncResult.failure("ERR001", "Connection timeout"));

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(crawledProductReadManager).should().findById(PRODUCT_ID);
            then(syncOutboxManager).should().markAsProcessing(outbox);
            then(externalProductServerClient).should().createProduct(any(), any());
            then(syncCompletionFacade).should().failSync(eq(outbox), anyString());
            then(syncCompletionFacade).should(never()).completeSync(any(), any(), any());
        }

        @Test
        @DisplayName("[실패] 처리 중 예외 발생 → FAILED 상태로 변경")
        void shouldMarkAsFailedWhenExceptionOccurs() {
            // Given
            ExternalSyncRequestedEvent event =
                    createEvent(ExternalSyncRequestedEvent.SyncType.CREATE);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            CrawledProduct product = createSyncReadyProduct(null);

            given(syncOutboxReadManager.findByIdempotencyKey(IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(externalProductServerClient.createProduct(any(), any()))
                    .willThrow(new RuntimeException("Unexpected error"));

            // When
            listener.handleExternalSyncRequested(event);

            // Then
            then(syncOutboxReadManager).should().findByIdempotencyKey(IDEMPOTENCY_KEY);
            then(crawledProductReadManager).should().findById(PRODUCT_ID);
            then(syncOutboxManager).should().markAsProcessing(outbox);
            then(externalProductServerClient).should().createProduct(any(), any());
            then(syncCompletionFacade).should().failSync(eq(outbox), anyString());
            then(syncCompletionFacade).should(never()).completeSync(any(), any(), any());
        }
    }

    // === Helper Methods ===

    private ExternalSyncRequestedEvent createEvent(ExternalSyncRequestedEvent.SyncType syncType) {
        return new ExternalSyncRequestedEvent(
                PRODUCT_ID, SELLER_ID, ITEM_NO, IDEMPOTENCY_KEY, syncType, FIXED_CLOCK.instant());
    }

    private CrawledProductSyncOutbox createUpdateOutbox() {
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                CrawledProductSyncOutbox.SyncType.UPDATE,
                IDEMPOTENCY_KEY,
                EXTERNAL_PRODUCT_ID,
                com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus.PENDING,
                0,
                null,
                FIXED_CLOCK.instant(),
                null);
    }

    private CrawledProduct createSyncReadyProduct(Long externalProductId) {
        Instant now = FIXED_CLOCK.instant();
        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "Test Product",
                "Test Brand",
                ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.empty(),
                createAllCrawledStatus(now),
                externalProductId,
                null,
                true,
                now,
                now);
    }

    private CrawlCompletionStatus createAllCrawledStatus(Instant now) {
        return CrawlCompletionStatus.initial()
                .withMiniShopCrawled(now)
                .withDetailCrawled(now)
                .withOptionCrawled(now);
    }
}
