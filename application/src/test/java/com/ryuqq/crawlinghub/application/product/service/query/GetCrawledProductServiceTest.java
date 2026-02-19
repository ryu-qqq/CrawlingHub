package com.ryuqq.crawlinghub.application.product.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetCrawledProductService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCrawledProductService 테스트")
class GetCrawledProductServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductQueryPort crawledProductQueryPort;

    private GetCrawledProductService service;

    @BeforeEach
    void setUp() {
        service = new GetCrawledProductService(crawledProductQueryPort);
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] 상품 존재 시 → Optional.of(product) 반환")
        void shouldReturnProductWhenExists() {
            // Given
            CrawledProduct product = createMockProduct();
            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.of(product));

            // When
            Optional<CrawledProduct> result = service.findById(PRODUCT_ID);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(PRODUCT_ID);
            then(crawledProductQueryPort).should().findById(PRODUCT_ID);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 시 → Optional.empty() 반환")
        void shouldReturnEmptyWhenNotExists() {
            // Given
            given(crawledProductQueryPort.findById(PRODUCT_ID)).willReturn(Optional.empty());

            // When
            Optional<CrawledProduct> result = service.findById(PRODUCT_ID);

            // Then
            assertThat(result).isEmpty();
            then(crawledProductQueryPort).should().findById(PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("findBySellerIdAndItemNo() 테스트")
    class FindBySellerIdAndItemNo {

        @Test
        @DisplayName("[성공] 상품 존재 시 → Optional.of(product) 반환")
        void shouldReturnProductWhenExists() {
            // Given
            CrawledProduct product = createMockProduct();
            given(crawledProductQueryPort.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(product));

            // When
            Optional<CrawledProduct> result = service.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSellerId()).isEqualTo(SELLER_ID);
            assertThat(result.get().getItemNo()).isEqualTo(ITEM_NO);
            then(crawledProductQueryPort).should().findBySellerIdAndItemNo(SELLER_ID, ITEM_NO);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 시 → Optional.empty() 반환")
        void shouldReturnEmptyWhenNotExists() {
            // Given
            given(crawledProductQueryPort.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.empty());

            // When
            Optional<CrawledProduct> result = service.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO);

            // Then
            assertThat(result).isEmpty();
            then(crawledProductQueryPort).should().findBySellerIdAndItemNo(SELLER_ID, ITEM_NO);
        }
    }

    @Nested
    @DisplayName("findBySellerId() 테스트")
    class FindBySellerId {

        @Test
        @DisplayName("[성공] 상품 존재 시 → 목록 반환")
        void shouldReturnProductsWhenExist() {
            // Given
            CrawledProduct product1 = createMockProduct();
            CrawledProduct product2 = createMockProductWithId(CrawledProductId.of(2L));
            List<CrawledProduct> products = List.of(product1, product2);

            given(crawledProductQueryPort.findBySellerId(SELLER_ID)).willReturn(products);

            // When
            List<CrawledProduct> result = service.findBySellerId(SELLER_ID);

            // Then
            assertThat(result).hasSize(2);
            then(crawledProductQueryPort).should().findBySellerId(SELLER_ID);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 시 → 빈 목록 반환")
        void shouldReturnEmptyListWhenNoProductsExist() {
            // Given
            given(crawledProductQueryPort.findBySellerId(SELLER_ID))
                    .willReturn(Collections.emptyList());

            // When
            List<CrawledProduct> result = service.findBySellerId(SELLER_ID);

            // Then
            assertThat(result).isEmpty();
            then(crawledProductQueryPort).should().findBySellerId(SELLER_ID);
        }
    }

    // === Helper Methods ===

    private CrawledProduct createMockProduct() {
        return createMockProductWithId(PRODUCT_ID);
    }

    private CrawledProduct createMockProductWithId(CrawledProductId productId) {
        Instant now = FIXED_INSTANT;
        return CrawledProduct.reconstitute(
                productId,
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
                CrawlCompletionStatus.initial().withMiniShopCrawled(now),
                null,
                null,
                false,
                now,
                now);
    }
}
