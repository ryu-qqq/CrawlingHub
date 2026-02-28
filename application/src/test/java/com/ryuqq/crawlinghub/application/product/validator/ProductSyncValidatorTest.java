package com.ryuqq.crawlinghub.application.product.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.validator.ProductSyncValidator.SyncTarget;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProductSyncValidator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSyncValidator 테스트")
class ProductSyncValidatorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductSyncOutboxReadManager syncOutboxReadManager;
    @Mock private CrawledProductReadManager crawledProductReadManager;
    @Mock private CrawledProductSyncOutboxCommandManager syncOutboxCommandManager;

    private ProductSyncValidator validator;

    @BeforeEach
    void setUp() {
        validator =
                new ProductSyncValidator(
                        syncOutboxReadManager, crawledProductReadManager, syncOutboxCommandManager);
    }

    @Nested
    @DisplayName("validateAndResolve() 테스트")
    class ValidateAndResolve {

        @Test
        @DisplayName("[실패] Outbox 미존재 → empty 반환")
        void shouldReturnEmptyWhenOutboxNotFound() {
            // Given
            given(syncOutboxReadManager.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<SyncTarget> result = validator.validateAndResolve(999L);

            // Then
            assertThat(result).isEmpty();
            verify(syncOutboxCommandManager, never())
                    .markAsFailed(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("[실패] Outbox PROCESSING 상태 → empty 반환 (skip)")
        void shouldReturnEmptyWhenOutboxIsProcessing() {
            // Given
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.PROCESSING);
            given(syncOutboxReadManager.findById(1L)).willReturn(Optional.of(outbox));

            // When
            Optional<SyncTarget> result = validator.validateAndResolve(1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] Outbox COMPLETED 상태 → empty 반환 (skip)")
        void shouldReturnEmptyWhenOutboxIsCompleted() {
            // Given
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.COMPLETED);
            given(syncOutboxReadManager.findById(1L)).willReturn(Optional.of(outbox));

            // When
            Optional<SyncTarget> result = validator.validateAndResolve(1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] Product 미존재 → Outbox FAILED 처리 + empty 반환")
        void shouldMarkAsFailedAndReturnEmptyWhenProductNotFound() {
            // Given
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.SENT);
            given(syncOutboxReadManager.findById(1L)).willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(outbox.getCrawledProductId()))
                    .willReturn(Optional.empty());

            // When
            Optional<SyncTarget> result = validator.validateAndResolve(1L);

            // Then
            assertThat(result).isEmpty();
            verify(syncOutboxCommandManager, times(1))
                    .markAsFailed(outbox, "CrawledProduct를 찾을 수 없음");
        }

        @Test
        @DisplayName("[성공] Outbox SENT + Product 존재 → SyncTarget 반환")
        void shouldReturnSyncTargetWhenValid() {
            // Given
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.SENT);
            CrawledProduct product = createMockProduct();
            given(syncOutboxReadManager.findById(1L)).willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(outbox.getCrawledProductId()))
                    .willReturn(Optional.of(product));

            // When
            Optional<SyncTarget> result = validator.validateAndResolve(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().outbox()).isEqualTo(outbox);
            assertThat(result.get().product()).isEqualTo(product);
        }

        @Test
        @DisplayName("[성공] Outbox PENDING + Product 존재 → SyncTarget 반환")
        void shouldReturnSyncTargetWhenPendingStatus() {
            // Given
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.PENDING);
            CrawledProduct product = createMockProduct();
            given(syncOutboxReadManager.findById(1L)).willReturn(Optional.of(outbox));
            given(crawledProductReadManager.findById(outbox.getCrawledProductId()))
                    .willReturn(Optional.of(product));

            // When
            Optional<SyncTarget> result = validator.validateAndResolve(1L);

            // Then
            assertThat(result).isPresent();
        }
    }

    // === Helper Methods ===

    private CrawledProductSyncOutbox createOutbox(ProductOutboxStatus status) {
        return CrawledProductSyncOutbox.reconstitute(
                CrawledProductSyncOutboxId.of(1L),
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                SyncType.CREATE,
                "sync-key-123",
                null,
                status,
                0,
                null,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }

    private CrawledProduct createMockProduct() {
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
                        .withMiniShopCrawled(FIXED_INSTANT)
                        .withDetailCrawled(FIXED_INSTANT)
                        .withOptionCrawled(FIXED_INSTANT),
                null,
                null,
                true,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT,
                null);
    }
}
