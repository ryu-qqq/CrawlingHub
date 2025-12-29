package com.ryuqq.crawlinghub.application.sync.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
import com.ryuqq.crawlinghub.application.product.factory.SyncOutboxFactory;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RequestSyncService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RequestSyncService 테스트")
class RequestSyncServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));

    @Mock private SyncOutboxFactory syncOutboxFactory;

    @Mock private SyncOutboxTransactionManager syncOutboxTransactionManager;

    @Mock private TransactionEventRegistry eventRegistry;

    @InjectMocks private RequestSyncService service;

    @Nested
    @DisplayName("requestIfReady() 동기화 요청 테스트")
    class RequestIfReady {

        @Test
        @DisplayName("[성공] 신규 상품 - 동기화 조건 충족 시 CREATE 타입으로 Bundle 생성 및 처리")
        void shouldCreateBundleAndProcessForNewProduct() {
            // Given
            CrawledProduct product = createSyncReadyProduct(null);
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();
            ExternalSyncRequestedEvent event =
                    new ExternalSyncRequestedEvent(
                            outbox.getCrawledProductId(),
                            outbox.getSellerId(),
                            outbox.getItemNo(),
                            outbox.getIdempotencyKey(),
                            ExternalSyncRequestedEvent.SyncType.CREATE,
                            FIXED_CLOCK.instant());
            SyncOutboxBundle bundle = new SyncOutboxBundle(outbox, event);

            given(syncOutboxFactory.createBundle(any())).willReturn(Optional.of(bundle));

            // When
            service.requestIfReady(product);

            // Then
            then(syncOutboxFactory).should().createBundle(product);
            then(syncOutboxTransactionManager).should().persist(bundle);

            ArgumentCaptor<ExternalSyncRequestedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ExternalSyncRequestedEvent.class);
            then(eventRegistry).should().registerForPublish(eventCaptor.capture());

            ExternalSyncRequestedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.syncType())
                    .isEqualTo(ExternalSyncRequestedEvent.SyncType.CREATE);
            assertThat(capturedEvent.crawledProductId()).isEqualTo(outbox.getCrawledProductId());
            assertThat(capturedEvent.sellerId()).isEqualTo(outbox.getSellerId());
            assertThat(capturedEvent.itemNo()).isEqualTo(outbox.getItemNo());
        }

        @Test
        @DisplayName("[성공] 기존 상품 - 동기화 조건 충족 시 UPDATE 타입으로 Bundle 생성 및 처리")
        void shouldCreateBundleAndProcessForExistingProduct() {
            // Given
            Long externalProductId = 99999L;
            CrawledProduct product = createSyncReadyProduct(externalProductId);
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForUpdate();
            ExternalSyncRequestedEvent event =
                    new ExternalSyncRequestedEvent(
                            outbox.getCrawledProductId(),
                            outbox.getSellerId(),
                            outbox.getItemNo(),
                            outbox.getIdempotencyKey(),
                            ExternalSyncRequestedEvent.SyncType.UPDATE,
                            FIXED_CLOCK.instant());
            SyncOutboxBundle bundle = new SyncOutboxBundle(outbox, event);

            given(syncOutboxFactory.createBundle(any())).willReturn(Optional.of(bundle));

            // When
            service.requestIfReady(product);

            // Then
            then(syncOutboxFactory).should().createBundle(product);
            then(syncOutboxTransactionManager).should().persist(bundle);

            ArgumentCaptor<ExternalSyncRequestedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ExternalSyncRequestedEvent.class);
            then(eventRegistry).should().registerForPublish(eventCaptor.capture());

            ExternalSyncRequestedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.syncType())
                    .isEqualTo(ExternalSyncRequestedEvent.SyncType.UPDATE);
        }

        @Test
        @DisplayName("[스킵] 중복 Outbox 존재 시 persist/이벤트 발행 안 함")
        void shouldSkipWhenDuplicateOutboxExists() {
            // Given
            CrawledProduct product = createSyncReadyProduct(null);
            given(syncOutboxFactory.createBundle(any())).willReturn(Optional.empty());

            // When
            service.requestIfReady(product);

            // Then
            then(syncOutboxFactory).should().createBundle(product);
            verifyNoInteractions(syncOutboxTransactionManager, eventRegistry);
        }

        @Test
        @DisplayName("[스킵] needsSync=false인 경우 Factory 호출 안 함")
        void shouldSkipWhenNeedsSyncIsFalse() {
            // Given
            CrawledProduct product = createNotNeedsSyncProduct();

            // When
            service.requestIfReady(product);

            // Then
            verifyNoInteractions(syncOutboxFactory, syncOutboxTransactionManager, eventRegistry);
        }

        @Test
        @DisplayName("[스킵] 크롤링 미완료 시 Factory 호출 안 함")
        void shouldSkipWhenCrawlingNotCompleted() {
            // Given
            CrawledProduct product = createCrawlingIncompleteProduct();

            // When
            service.requestIfReady(product);

            // Then
            verifyNoInteractions(syncOutboxFactory, syncOutboxTransactionManager, eventRegistry);
        }
    }

    // === Helper Methods ===

    /** 동기화 준비 완료된 CrawledProduct 생성 - MINI_SHOP, DETAIL, OPTION 모두 크롤링 완료 - needsSync = true */
    private CrawledProduct createSyncReadyProduct(Long externalProductId) {
        Instant now = FIXED_CLOCK.instant();
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
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
                true, // needsSync = true
                now,
                now);
    }

    /** needsSync=false인 CrawledProduct 생성 */
    private CrawledProduct createNotNeedsSyncProduct() {
        Instant now = FIXED_CLOCK.instant();
        return CrawledProduct.reconstitute(
                CrawledProductId.of(2L),
                SellerId.of(100L),
                12346L,
                "Test Product 2",
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
                null,
                null,
                false, // needsSync = false
                now,
                now);
    }

    /** 크롤링 미완료 CrawledProduct 생성 (MINI_SHOP만 완료) */
    private CrawledProduct createCrawlingIncompleteProduct() {
        Instant now = FIXED_CLOCK.instant();
        return CrawledProduct.reconstitute(
                CrawledProductId.of(3L),
                SellerId.of(100L),
                12347L,
                "Test Product 3",
                "Test Brand",
                ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                CrawlCompletionStatus.initial().withMiniShopCrawled(now), // MINI_SHOP만 완료
                null,
                null,
                true, // needsSync = true이지만 canSyncToExternalServer() = false
                now,
                now);
    }

    /** 모든 크롤링 완료 상태 생성 */
    private CrawlCompletionStatus createAllCrawledStatus(Instant now) {
        return CrawlCompletionStatus.initial()
                .withMiniShopCrawled(now)
                .withDetailCrawled(now)
                .withOptionCrawled(now);
    }
}
