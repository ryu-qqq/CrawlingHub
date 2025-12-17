package com.ryuqq.crawlinghub.application.product.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SyncOutboxFactory 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SyncOutboxFactory 테스트")
class SyncOutboxFactoryTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));

    private SyncOutboxFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SyncOutboxFactory(FIXED_CLOCK);
    }

    @Nested
    @DisplayName("createBundle() 테스트")
    class CreateBundle {

        @Test
        @DisplayName("[성공] 신규 상품 → CREATE 타입 Bundle 생성")
        void shouldCreateBundleWithCreateTypeForNewProduct() {
            // Given
            CrawledProduct product = createSyncReadyProduct(null);

            // When
            SyncOutboxBundle bundle = factory.createBundle(product);

            // Then
            assertThat(bundle).isNotNull();
            assertThat(bundle.isCreateRequest()).isTrue();
            assertThat(bundle.isUpdateRequest()).isFalse();

            CrawledProductSyncOutbox outbox = bundle.outbox();
            assertThat(outbox.getCrawledProductId()).isEqualTo(product.getId());
            assertThat(outbox.getSellerId()).isEqualTo(product.getSellerId());
            assertThat(outbox.getItemNo()).isEqualTo(product.getItemNo());
            assertThat(outbox.getSyncType()).isEqualTo(CrawledProductSyncOutbox.SyncType.CREATE);
            assertThat(outbox.getExternalProductId()).isNull();

            ExternalSyncRequestedEvent event = bundle.event();
            assertThat(event.syncType()).isEqualTo(ExternalSyncRequestedEvent.SyncType.CREATE);
            assertThat(event.crawledProductId()).isEqualTo(product.getId());
            assertThat(event.sellerId()).isEqualTo(product.getSellerId());
            assertThat(event.idempotencyKey()).isEqualTo(outbox.getIdempotencyKey());
        }

        @Test
        @DisplayName("[성공] 기존 상품 → UPDATE 타입 Bundle 생성")
        void shouldCreateBundleWithUpdateTypeForExistingProduct() {
            // Given
            Long externalProductId = 99999L;
            CrawledProduct product = createSyncReadyProduct(externalProductId);

            // When
            SyncOutboxBundle bundle = factory.createBundle(product);

            // Then
            assertThat(bundle).isNotNull();
            assertThat(bundle.isCreateRequest()).isFalse();
            assertThat(bundle.isUpdateRequest()).isTrue();

            CrawledProductSyncOutbox outbox = bundle.outbox();
            assertThat(outbox.getSyncType()).isEqualTo(CrawledProductSyncOutbox.SyncType.UPDATE);
            assertThat(outbox.getExternalProductId()).isEqualTo(externalProductId);

            ExternalSyncRequestedEvent event = bundle.event();
            assertThat(event.syncType()).isEqualTo(ExternalSyncRequestedEvent.SyncType.UPDATE);
            assertThat(event.idempotencyKey()).isEqualTo(outbox.getIdempotencyKey());
        }

        @Test
        @DisplayName("[실패] needsSync=false인 경우 예외 발생")
        void shouldThrowExceptionWhenNeedsSyncIsFalse() {
            // Given
            CrawledProduct product = createNotNeedsSyncProduct();

            // When & Then
            assertThatThrownBy(() -> factory.createBundle(product))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not ready for sync");
        }

        @Test
        @DisplayName("[실패] 크롤링 미완료 시 예외 발생")
        void shouldThrowExceptionWhenCrawlingNotCompleted() {
            // Given
            CrawledProduct product = createCrawlingIncompleteProduct();

            // When & Then
            assertThatThrownBy(() -> factory.createBundle(product))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not ready for sync");
        }
    }

    // === Helper Methods ===

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
                true,
                now,
                now);
    }

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
                false,
                now,
                now);
    }

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
                CrawlCompletionStatus.initial().withMiniShopCrawled(now),
                null,
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
