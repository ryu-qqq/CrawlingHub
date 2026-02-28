package com.ryuqq.crawlinghub.application.product.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductSyncOutboxFactory 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawledProductSyncOutboxFactory 테스트")
class CrawledProductSyncOutboxFactoryTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    private CrawledProductSyncOutboxFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CrawledProductSyncOutboxFactory();
    }

    @Nested
    @DisplayName("createAll() 테스트")
    class CreateAll {

        @Test
        @DisplayName("[성공] 신규 상품 → CREATE 타입 Outbox 1건 생성")
        void shouldCreateSingleCreateOutboxForNewProduct() {
            // Given
            CrawledProduct product = createNewProduct();

            // When
            List<CrawledProductSyncOutbox> outboxes = factory.createAll(product);

            // Then
            assertThat(outboxes).hasSize(1);
            CrawledProductSyncOutbox outbox = outboxes.get(0);
            assertThat(outbox.getCrawledProductId()).isEqualTo(product.getId());
            assertThat(outbox.getSellerId()).isEqualTo(product.getSellerId());
            assertThat(outbox.getItemNo()).isEqualTo(product.getItemNo());
            assertThat(outbox.getSyncType()).isEqualTo(CrawledProductSyncOutbox.SyncType.CREATE);
            assertThat(outbox.getExternalProductId()).isNull();
            assertThat(outbox.isPending()).isTrue();
        }

        @Test
        @DisplayName("[성공] 기존 상품 + PRICE 변경 → UPDATE_PRICE 타입 Outbox 생성")
        void shouldCreateUpdatePriceOutboxForExistingProductWithPriceChange() {
            // Given
            Long externalProductId = 99999L;
            Set<ProductChangeType> pendingChanges = EnumSet.of(ProductChangeType.PRICE);
            CrawledProduct product = createExistingProduct(externalProductId, pendingChanges);

            // When
            List<CrawledProductSyncOutbox> outboxes = factory.createAll(product);

            // Then
            assertThat(outboxes).hasSize(1);
            CrawledProductSyncOutbox outbox = outboxes.get(0);
            assertThat(outbox.getSyncType())
                    .isEqualTo(CrawledProductSyncOutbox.SyncType.UPDATE_PRICE);
            assertThat(outbox.getExternalProductId()).isEqualTo(externalProductId);
        }
    }

    // === Helper Methods ===

    private CrawledProduct createNewProduct() {
        Instant now = FIXED_INSTANT;
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "Test Product",
                "Test Brand",
                0L,
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
                true,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createExistingProduct(
            Long externalProductId, Set<ProductChangeType> pendingChanges) {
        Instant now = FIXED_INSTANT;
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "Test Product",
                "Test Brand",
                0L,
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
                pendingChanges,
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawlCompletionStatus createAllCrawledStatus(Instant now) {
        return CrawlCompletionStatus.initial()
                .withMiniShopCrawled(now)
                .withDetailCrawled(now)
                .withOptionCrawled(now);
    }
}
