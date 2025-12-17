package com.ryuqq.crawlinghub.application.product.facade;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SyncCompletionFacade 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SyncCompletionFacade 테스트")
class SyncCompletionFacadeTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;
    private static final Long EXTERNAL_PRODUCT_ID = 99999L;

    @Mock private SyncOutboxManager syncOutboxManager;

    @Mock private CrawledProductTransactionManager crawledProductManager;

    private SyncCompletionFacade facade;

    @BeforeEach
    void setUp() {
        facade = new SyncCompletionFacade(syncOutboxManager, crawledProductManager);
    }

    @Nested
    @DisplayName("completeSync() 테스트")
    class CompleteSync {

        @Test
        @DisplayName("[성공] CREATE 요청 완료 → Outbox COMPLETED + Product에 externalProductId 저장")
        void shouldCompleteSyncForCreateRequest() {
            // Given
            CrawledProductSyncOutbox outbox = createCreateOutbox();
            CrawledProduct product = createMockProduct(null);
            Long newExternalProductId = 12345L;

            // When
            facade.completeSync(outbox, product, newExternalProductId);

            // Then
            // Outbox COMPLETED 상태로 변경 검증
            verify(syncOutboxManager, times(1))
                    .markAsCompleted(eq(outbox), eq(newExternalProductId));

            // CREATE 요청이므로 새로운 externalProductId로 저장
            verify(crawledProductManager, times(1))
                    .markAsSynced(eq(product), eq(newExternalProductId));
        }

        @Test
        @DisplayName("[성공] UPDATE 요청 완료 → Outbox COMPLETED + 기존 externalProductId 유지")
        void shouldCompleteSyncForUpdateRequest() {
            // Given
            CrawledProductSyncOutbox outbox = createUpdateOutbox();
            CrawledProduct product = createMockProduct(EXTERNAL_PRODUCT_ID);
            Long returnedExternalProductId = EXTERNAL_PRODUCT_ID;

            // When
            facade.completeSync(outbox, product, returnedExternalProductId);

            // Then
            // Outbox COMPLETED 상태로 변경 검증
            verify(syncOutboxManager, times(1))
                    .markAsCompleted(eq(outbox), eq(returnedExternalProductId));

            // UPDATE 요청이므로 기존 externalProductId 유지
            verify(crawledProductManager, times(1))
                    .markAsSynced(eq(product), eq(EXTERNAL_PRODUCT_ID));
        }
    }

    @Nested
    @DisplayName("failSync() 테스트")
    class FailSync {

        @Test
        @DisplayName("[성공] 동기화 실패 → Outbox FAILED 상태로 변경")
        void shouldMarkOutboxAsFailedOnSyncFailure() {
            // Given
            CrawledProductSyncOutbox outbox = createCreateOutbox();
            String errorMessage = "Connection timeout";

            // When
            facade.failSync(outbox, errorMessage);

            // Then
            verify(syncOutboxManager, times(1)).markAsFailed(eq(outbox), eq(errorMessage));
        }

        @Test
        @DisplayName("[성공] 재시도 가능한 실패 → Outbox FAILED + 재시도 카운트 증가 예정")
        void shouldMarkOutboxAsFailedForRetryableError() {
            // Given
            CrawledProductSyncOutbox outbox = createProcessingOutbox();
            String errorMessage = "Server temporarily unavailable";

            // When
            facade.failSync(outbox, errorMessage);

            // Then
            verify(syncOutboxManager, times(1)).markAsFailed(eq(outbox), eq(errorMessage));
        }
    }

    // === Helper Methods ===

    private CrawledProductSyncOutbox createCreateOutbox() {
        return CrawledProductSyncOutbox.reconstitute(
                1L,
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                SyncType.CREATE,
                "sync-key-create-123",
                null,
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                FIXED_CLOCK.instant(),
                FIXED_CLOCK.instant());
    }

    private CrawledProductSyncOutbox createUpdateOutbox() {
        return CrawledProductSyncOutbox.reconstitute(
                2L,
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                SyncType.UPDATE,
                "sync-key-update-123",
                EXTERNAL_PRODUCT_ID,
                ProductOutboxStatus.PROCESSING,
                0,
                null,
                FIXED_CLOCK.instant(),
                FIXED_CLOCK.instant());
    }

    private CrawledProductSyncOutbox createProcessingOutbox() {
        return CrawledProductSyncOutbox.reconstitute(
                3L,
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                SyncType.CREATE,
                "sync-key-processing-123",
                null,
                ProductOutboxStatus.PROCESSING,
                1,
                null,
                FIXED_CLOCK.instant(),
                FIXED_CLOCK.instant());
    }

    private CrawledProduct createMockProduct(Long externalProductId) {
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
                ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                null,
                "<p>Test Description</p>",
                "<p>Test Description</p>",
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.empty(),
                CrawlCompletionStatus.initial()
                        .withMiniShopCrawled(now)
                        .withDetailCrawled(now)
                        .withOptionCrawled(now),
                externalProductId,
                null,
                true,
                now,
                now);
    }
}
