package com.ryuqq.crawlinghub.application.sync.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.facade.SyncCompletionFacade;
import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.application.sync.dto.command.SyncRetryResult;
import com.ryuqq.crawlinghub.application.sync.manager.SyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RetrySyncService 단위 테스트
 *
 * <p>외부 서버 동기화 재시도 로직 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetrySyncService 테스트")
class RetrySyncServiceTest {

    @Mock private SyncOutboxReadManager syncOutboxReadManager;

    @Mock private SyncOutboxManager syncOutboxManager;

    @Mock private CrawledProductReadManager crawledProductReadManager;

    @Mock private SyncCompletionFacade syncCompletionFacade;

    @Mock private ExternalProductServerClient externalProductServerClient;

    @InjectMocks private RetrySyncService retrySyncService;

    private static final Clock FIXED_CLOCK = FixedClock.aDefaultClock();
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;
    private static final Long EXTERNAL_PRODUCT_ID = 99999L;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 재시도 대상 없음 → 빈 결과 반환")
        void shouldReturnEmptyResultWhenNoRetryableOutboxes() {
            // Given
            given(syncOutboxReadManager.findRetryableOutboxes(3, 100))
                    .willReturn(Collections.emptyList());

            // When
            SyncRetryResult result = retrySyncService.execute();

            // Then
            assertThat(result.processed()).isZero();
            assertThat(result.succeeded()).isZero();
            assertThat(result.failed()).isZero();
            assertThat(result.hasMore()).isFalse();

            then(syncOutboxReadManager).should().findRetryableOutboxes(3, 100);
            then(syncOutboxManager).shouldHaveNoInteractions();
            then(externalProductServerClient).shouldHaveNoInteractions();
            then(syncCompletionFacade).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[성공] 재시도 성공 → 성공 건수 증가")
        void shouldIncrementSucceededCountOnSuccess() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            CrawledProduct product = createSyncReadyProduct(null);

            given(syncOutboxReadManager.findRetryableOutboxes(3, 100)).willReturn(List.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(externalProductServerClient.createProduct(any(), any()))
                    .willReturn(ProductSyncResult.success(EXTERNAL_PRODUCT_ID));

            // When
            SyncRetryResult result = retrySyncService.execute();

            // Then
            assertThat(result.processed()).isEqualTo(1);
            assertThat(result.succeeded()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            assertThat(result.hasMore()).isFalse();

            then(syncOutboxManager).should().markAsProcessing(outbox);
            then(syncCompletionFacade).should().completeSync(outbox, product, EXTERNAL_PRODUCT_ID);
        }

        @Test
        @DisplayName("[실패] CrawledProduct 미존재 → 실패 건수 증가")
        void shouldIncrementFailedCountWhenProductNotFound() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            given(syncOutboxReadManager.findRetryableOutboxes(3, 100)).willReturn(List.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.empty());

            // When
            SyncRetryResult result = retrySyncService.execute();

            // Then
            assertThat(result.processed()).isEqualTo(1);
            assertThat(result.succeeded()).isZero();
            assertThat(result.failed()).isEqualTo(1);

            then(syncCompletionFacade).should().failSync(eq(outbox), anyString());
            then(syncOutboxManager).should(never()).markAsProcessing(any());
        }

        @Test
        @DisplayName("[실패] 외부 API 호출 실패 → 실패 건수 증가")
        void shouldIncrementFailedCountWhenApiCallFails() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            CrawledProduct product = createSyncReadyProduct(null);

            given(syncOutboxReadManager.findRetryableOutboxes(3, 100)).willReturn(List.of(outbox));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(externalProductServerClient.createProduct(any(), any()))
                    .willReturn(ProductSyncResult.failure("ERR001", "Connection timeout"));

            // When
            SyncRetryResult result = retrySyncService.execute();

            // Then
            assertThat(result.processed()).isEqualTo(1);
            assertThat(result.succeeded()).isZero();
            assertThat(result.failed()).isEqualTo(1);

            then(syncOutboxManager).should().markAsProcessing(outbox);
            then(syncCompletionFacade).should().failSync(eq(outbox), anyString());
        }

        @Test
        @DisplayName("[복합] 여러 건 처리 → 성공/실패 각각 집계")
        void shouldAggregateMixedResults() {
            // Given
            CrawledProductSyncOutbox outbox1 =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            CrawledProductSyncOutbox outbox2 =
                    CrawledProductSyncOutbox.reconstitute(
                            2L,
                            CrawledProductId.of(2L),
                            SELLER_ID,
                            ITEM_NO + 1,
                            CrawledProductSyncOutbox.SyncType.CREATE,
                            "sync-2-create-def67890",
                            null,
                            com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus.FAILED,
                            1,
                            "Previous error",
                            FIXED_CLOCK.instant(),
                            null);

            CrawledProduct product1 = createSyncReadyProduct(null);

            given(syncOutboxReadManager.findRetryableOutboxes(3, 100))
                    .willReturn(List.of(outbox1, outbox2));
            given(crawledProductReadManager.findById(PRODUCT_ID)).willReturn(Optional.of(product1));
            given(crawledProductReadManager.findById(CrawledProductId.of(2L)))
                    .willReturn(Optional.empty());
            given(externalProductServerClient.createProduct(any(), any()))
                    .willReturn(ProductSyncResult.success(EXTERNAL_PRODUCT_ID));

            // When
            SyncRetryResult result = retrySyncService.execute();

            // Then
            assertThat(result.processed()).isEqualTo(2);
            assertThat(result.succeeded()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);

            then(syncOutboxManager).should(times(1)).markAsProcessing(any());
            then(syncCompletionFacade).should(times(1)).completeSync(any(), any(), any());
            then(syncCompletionFacade).should(times(1)).failSync(any(), anyString());
        }

        @Test
        @DisplayName("[페이징] 배치 크기만큼 조회 시 hasMore=true")
        void shouldSetHasMoreTrueWhenBatchSizeReached() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            List<CrawledProductSyncOutbox> batchList = Collections.nCopies(100, outbox);
            CrawledProduct product = createSyncReadyProduct(null);

            given(syncOutboxReadManager.findRetryableOutboxes(3, 100)).willReturn(batchList);
            given(crawledProductReadManager.findById(any())).willReturn(Optional.of(product));
            given(externalProductServerClient.createProduct(any(), any()))
                    .willReturn(ProductSyncResult.success(EXTERNAL_PRODUCT_ID));

            // When
            SyncRetryResult result = retrySyncService.execute();

            // Then
            assertThat(result.processed()).isEqualTo(100);
            assertThat(result.hasMore()).isTrue();
        }
    }

    // === Helper Methods ===

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
