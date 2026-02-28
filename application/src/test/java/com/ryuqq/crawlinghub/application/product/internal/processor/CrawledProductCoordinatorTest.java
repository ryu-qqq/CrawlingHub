package com.ryuqq.crawlinghub.application.product.internal.processor;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.product.internal.CrawledProductCommandFacade;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductCoordinator;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductReadManager;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
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
 * CrawledProductCoordinator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductCoordinator 테스트")
class CrawledProductCoordinatorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 10001L;

    @Mock private CrawledProductReadManager readManager;
    @Mock private CrawledProductCommandManager commandManager;
    @Mock private CrawledProductCommandFacade commandFacade;

    private CrawledProductCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new CrawledProductCoordinator(readManager, commandManager, commandFacade);
    }

    @Nested
    @DisplayName("updateExistingAndSync")
    class UpdateExistingAndSync {

        @Test
        @DisplayName("[성공] 상품 존재 → updater 실행 + persist + sync 호출")
        void shouldUpdateAndSyncWhenProductExists() {
            // Given
            CrawledProduct product = createMockProduct(true);
            given(readManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(product));

            // When
            coordinator.updateExistingAndSync(SELLER_ID, ITEM_NO, p -> {}); // updater는 도메인 테스트에서 검증

            // Then
            then(commandFacade).should(times(1)).persistAndSync(product);
        }

        @Test
        @DisplayName("[성공] 상품 존재 + needsExternalSync=false → persistAndSync 호출")
        void shouldPersistWithoutSyncWhenNotReady() {
            // Given
            CrawledProduct product = createMockProduct(false);
            given(readManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(product));

            // When
            coordinator.updateExistingAndSync(SELLER_ID, ITEM_NO, p -> {});

            // Then
            then(commandFacade).should(times(1)).persistAndSync(product);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 → 아무 동작 없음")
        void shouldDoNothingWhenProductNotExists() {
            // Given
            given(readManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.empty());

            // When
            coordinator.updateExistingAndSync(SELLER_ID, ITEM_NO, p -> {});

            // Then
            then(commandFacade).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("createOrUpdate")
    class CreateOrUpdate {

        @Test
        @DisplayName("[성공] 상품 존재 → updater 실행 + persist + sync 호출")
        void shouldUpdateWhenProductExists() {
            // Given
            CrawledProduct product = createMockProduct(true);
            given(readManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(product));

            // When
            coordinator.createOrUpdate(
                    SELLER_ID,
                    ITEM_NO,
                    p -> {},
                    () -> {
                        throw new AssertionError("creator가 호출되면 안 됨");
                    });

            // Then
            then(commandFacade).should(times(1)).persistAndSync(product);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 → creator로 생성 + persist (sync 미호출)")
        void shouldCreateWhenProductNotExists() {
            // Given
            CrawledProduct newProduct = createMockProduct(false);
            given(readManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.empty());
            given(readManager.findBySellerIdAndItemNoIncludingDeleted(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.empty());

            // When
            coordinator.createOrUpdate(
                    SELLER_ID,
                    ITEM_NO,
                    p -> {
                        throw new AssertionError("updater가 호출되면 안 됨");
                    },
                    () -> newProduct);

            // Then
            then(commandManager).should(times(1)).persist(newProduct);
        }

        @Test
        @DisplayName("[성공] 상품 존재 + needsExternalSync=false → persistAndSync 호출")
        void shouldUpdateWithoutSyncWhenNotReady() {
            // Given
            CrawledProduct product = createMockProduct(false);
            given(readManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(product));

            // When
            coordinator.createOrUpdate(SELLER_ID, ITEM_NO, p -> {}, () -> null);

            // Then
            then(commandFacade).should(times(1)).persistAndSync(product);
        }
    }

    private CrawledProduct createMockProduct(boolean allCrawled) {
        CrawlCompletionStatus status =
                allCrawled
                        ? CrawlCompletionStatus.initial()
                                .withMiniShopCrawled(FIXED_INSTANT)
                                .withDetailCrawled(FIXED_INSTANT)
                                .withOptionCrawled(FIXED_INSTANT)
                        : CrawlCompletionStatus.initial().withMiniShopCrawled(FIXED_INSTANT);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
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
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                allCrawled,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT,
                null);
    }
}
