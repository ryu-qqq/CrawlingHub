package com.ryuqq.crawlinghub.application.product.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledProductAssembler;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchCrawledProductsQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductSummaryResponse;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SearchCrawledProductsService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCrawledProductsService 테스트")
class SearchCrawledProductsServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductQueryPort queryPort;

    @Mock private CrawledProductAssembler assembler;

    private SearchCrawledProductsService service;

    @BeforeEach
    void setUp() {
        service = new SearchCrawledProductsService(queryPort, assembler);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 상품 존재 시 → PageResponse 반환")
        void shouldReturnPageResponseWhenProductsExist() {
            // Given
            SearchCrawledProductsQuery query =
                    new SearchCrawledProductsQuery(
                            SELLER_ID.value(), null, null, null, null, null, null, 0, 10);
            CrawledProduct product = createMockProduct();
            List<CrawledProduct> products = List.of(product);
            long totalElements = 1L;

            CrawledProductSummaryResponse summaryResponse = createMockSummaryResponse();

            given(queryPort.search(query)).willReturn(products);
            given(queryPort.count(query)).willReturn(totalElements);
            given(assembler.toSummaryResponse(product)).willReturn(summaryResponse);

            // When
            PageResponse<CrawledProductSummaryResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            then(queryPort).should().search(query);
            then(queryPort).should().count(query);
            then(assembler).should().toSummaryResponse(product);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 시 → 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponseWhenNoProductsFound() {
            // Given
            SearchCrawledProductsQuery query =
                    new SearchCrawledProductsQuery(999L, null, null, null, null, null, null, 0, 10);

            given(queryPort.search(query)).willReturn(Collections.emptyList());
            given(queryPort.count(query)).willReturn(0L);

            // When
            PageResponse<CrawledProductSummaryResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            then(queryPort).should().search(query);
            then(queryPort).should().count(query);
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 처리됨")
        void shouldHandlePagingCorrectly() {
            // Given
            int page = 2;
            int size = 20;
            long totalElements = 100L;
            SearchCrawledProductsQuery query =
                    new SearchCrawledProductsQuery(
                            null, null, null, null, null, null, null, page, size);
            CrawledProduct product = createMockProduct();
            List<CrawledProduct> products = List.of(product);
            CrawledProductSummaryResponse summaryResponse = createMockSummaryResponse();

            given(queryPort.search(query)).willReturn(products);
            given(queryPort.count(query)).willReturn(totalElements);
            given(assembler.toSummaryResponse(product)).willReturn(summaryResponse);

            // When
            PageResponse<CrawledProductSummaryResponse> result = service.execute(query);

            // Then
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.size()).isEqualTo(size);
            assertThat(result.totalElements()).isEqualTo(totalElements);
            assertThat(result.totalPages()).isEqualTo(5); // 100 / 20 = 5
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 판별 정확")
        void shouldCorrectlyDetermineLastPage() {
            // Given
            int page = 4; // last page (0-indexed, totalPages = 5)
            int size = 20;
            long totalElements = 100L;
            SearchCrawledProductsQuery query =
                    new SearchCrawledProductsQuery(
                            null, null, null, null, null, null, null, page, size);
            CrawledProduct product = createMockProduct();
            List<CrawledProduct> products = List.of(product);
            CrawledProductSummaryResponse summaryResponse = createMockSummaryResponse();

            given(queryPort.search(query)).willReturn(products);
            given(queryPort.count(query)).willReturn(totalElements);
            given(assembler.toSummaryResponse(product)).willReturn(summaryResponse);

            // When
            PageResponse<CrawledProductSummaryResponse> result = service.execute(query);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }
    }

    // === Helper Methods ===

    private CrawledProduct createMockProduct() {
        Instant now = FIXED_CLOCK.instant();
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

    private CrawledProductSummaryResponse createMockSummaryResponse() {
        Instant now = FIXED_CLOCK.instant();
        return new CrawledProductSummaryResponse(
                PRODUCT_ID.value(),
                SELLER_ID.value(),
                ITEM_NO,
                "Test Product",
                "Test Brand",
                10000,
                10,
                1,
                "DETAIL, OPTION",
                false,
                null,
                null,
                false,
                0,
                now,
                now);
    }
}
