package com.ryuqq.crawlinghub.application.sync.service.command;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CompleteSyncService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteSyncService 테스트")
class CompleteSyncServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;
    private static final Long EXTERNAL_PRODUCT_ID = 99999L;

    @Mock private CrawledProductQueryPort crawledProductQueryPort;

    @Mock private CrawledProductTransactionManager crawledProductManager;

    private CompleteSyncService service;

    @BeforeEach
    void setUp() {
        service = new CompleteSyncService(crawledProductQueryPort, crawledProductManager);
    }

    @Nested
    @DisplayName("complete() 테스트")
    class Complete {

        @Test
        @DisplayName("[성공] 상품 존재 → markAsSynced 호출")
        void shouldMarkAsSyncedWhenProductExists() {
            // Given
            CrawledProduct product = createMockProduct();

            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.of(product));

            // When
            service.complete(PRODUCT_ID, EXTERNAL_PRODUCT_ID);

            // Then
            verify(crawledProductManager, times(1))
                    .markAsSynced(eq(product), eq(EXTERNAL_PRODUCT_ID));
        }

        @Test
        @DisplayName("[성공] 상품 미존재 → markAsSynced 호출 안 함")
        void shouldNotMarkAsSyncedWhenProductNotExists() {
            // Given
            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.empty());

            // When
            service.complete(PRODUCT_ID, EXTERNAL_PRODUCT_ID);

            // Then
            verify(crawledProductManager, never())
                    .markAsSynced(
                            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    // === Helper Methods ===

    private CrawledProduct createMockProduct() {
        Instant now = FIXED_INSTANT;
        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "Test Product",
                "Test Brand",
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 10),
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
                CrawlCompletionStatus.initial()
                        .withMiniShopCrawled(now)
                        .withDetailCrawled(now)
                        .withOptionCrawled(now),
                null,
                null,
                true,
                now,
                now);
    }
}
