package com.ryuqq.crawlinghub.adapter.in.rest.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.query.SearchCrawledProductsApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchCrawledProductsQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductSummaryResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductQueryApiMapper 단위 테스트
 *
 * <p>CrawledProduct Query REST API ↔ Application Layer DTO 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>API Request → Application Query 변환
 *   <li>Application Response → API Response 변환
 *   <li>페이징 응답 변환
 *   <li>null 값 처리
 *   <li>시간 포맷 변환 (Instant → ISO String)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("CrawledProductQueryApiMapper 단위 테스트")
class CrawledProductQueryApiMapperTest {

    private CrawledProductQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawledProductQueryApiMapper();
    }

    @Nested
    @DisplayName("toQuery()는")
    class ToQuery {

        @Test
        @DisplayName("SearchCrawledProductsApiRequest를 SearchCrawledProductsQuery로 변환한다")
        void shouldConvertApiRequestToQuery() {
            // Given
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            1L, 100L, "상품명", "브랜드", true, false, true, 0, 20);

            // When
            SearchCrawledProductsQuery query = mapper.toQuery(request);

            // Then
            assertThat(query.sellerId()).isEqualTo(1L);
            assertThat(query.itemNo()).isEqualTo(100L);
            assertThat(query.itemName()).isEqualTo("상품명");
            assertThat(query.brandName()).isEqualTo("브랜드");
            assertThat(query.needsSync()).isTrue();
            assertThat(query.allCrawled()).isFalse();
            assertThat(query.hasExternalId()).isTrue();
            assertThat(query.page()).isZero();
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("null 필드가 포함된 요청도 정상 변환한다")
        void shouldHandleNullFields() {
            // Given
            SearchCrawledProductsApiRequest request =
                    new SearchCrawledProductsApiRequest(
                            null, null, null, null, null, null, null, null, null);

            // When
            SearchCrawledProductsQuery query = mapper.toQuery(request);

            // Then
            assertThat(query.sellerId()).isNull();
            assertThat(query.itemNo()).isNull();
            assertThat(query.itemName()).isNull();
            assertThat(query.brandName()).isNull();
            assertThat(query.needsSync()).isNull();
            assertThat(query.allCrawled()).isNull();
            assertThat(query.hasExternalId()).isNull();
            assertThat(query.page()).isZero(); // 기본값 적용
            assertThat(query.size()).isEqualTo(20); // 기본값 적용
        }
    }

    @Nested
    @DisplayName("toSummaryApiResponse()는")
    class ToSummaryApiResponse {

        @Test
        @DisplayName("CrawledProductSummaryResponse를 CrawledProductSummaryApiResponse로 변환한다")
        void shouldConvertSummaryResponseToApiResponse() {
            // Given
            Instant now = Instant.now();
            CrawledProductSummaryResponse appResponse =
                    new CrawledProductSummaryResponse(
                            1L, 100L, 12345L, "테스트 상품", "테스트 브랜드", 50000, 10, 2, "OPTION", true,
                            999L, now, true, 100, now, now);

            // When
            CrawledProductSummaryApiResponse apiResponse = mapper.toSummaryApiResponse(appResponse);

            // Then
            assertThat(apiResponse.id()).isEqualTo(1L);
            assertThat(apiResponse.sellerId()).isEqualTo(100L);
            assertThat(apiResponse.itemNo()).isEqualTo(12345L);
            assertThat(apiResponse.itemName()).isEqualTo("테스트 상품");
            assertThat(apiResponse.brandName()).isEqualTo("테스트 브랜드");
            assertThat(apiResponse.price()).isEqualTo(50000);
            assertThat(apiResponse.discountRate()).isEqualTo(10);
            assertThat(apiResponse.completedCrawlCount()).isEqualTo(2);
            assertThat(apiResponse.pendingCrawlTypes()).isEqualTo("OPTION");
            assertThat(apiResponse.needsSync()).isTrue();
            assertThat(apiResponse.externalProductId()).isEqualTo(999L);
            assertThat(apiResponse.lastSyncedAt()).isEqualTo(now.toString());
            assertThat(apiResponse.allImagesUploaded()).isTrue();
            assertThat(apiResponse.totalStock()).isEqualTo(100);
            assertThat(apiResponse.createdAt()).isEqualTo(now.toString());
            assertThat(apiResponse.updatedAt()).isEqualTo(now.toString());
        }

        @Test
        @DisplayName("Instant가 null이면 null로 변환한다")
        void shouldConvertNullInstantToNull() {
            // Given
            CrawledProductSummaryResponse appResponse =
                    new CrawledProductSummaryResponse(
                            1L, 100L, 12345L, "상품", "브랜드", 50000, 10, 2, null, false, null, null,
                            false, 0, null, null);

            // When
            CrawledProductSummaryApiResponse apiResponse = mapper.toSummaryApiResponse(appResponse);

            // Then
            assertThat(apiResponse.lastSyncedAt()).isNull();
            assertThat(apiResponse.createdAt()).isNull();
            assertThat(apiResponse.updatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDetailApiResponse()는")
    class ToDetailApiResponse {

        @Test
        @DisplayName("CrawledProductDetailResponse를 CrawledProductDetailApiResponse로 변환한다")
        void shouldConvertDetailResponseToApiResponse() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.id()).isEqualTo(1L);
            assertThat(apiResponse.sellerId()).isEqualTo(100L);
            assertThat(apiResponse.itemNo()).isEqualTo(12345L);
            assertThat(apiResponse.itemName()).isEqualTo("테스트 상품");
            assertThat(apiResponse.brandName()).isEqualTo("테스트 브랜드");
            assertThat(apiResponse.itemStatus()).isEqualTo("SALE");
            assertThat(apiResponse.originCountry()).isEqualTo("대한민국");
            assertThat(apiResponse.shippingLocation()).isEqualTo("서울");
            assertThat(apiResponse.freeShipping()).isTrue();
            assertThat(apiResponse.descriptionMarkUp()).isEqualTo("<p>상품 설명</p>");
            assertThat(apiResponse.createdAt()).isEqualTo(now.toString());
            assertThat(apiResponse.updatedAt()).isEqualTo(now.toString());
        }

        @Test
        @DisplayName("가격 정보를 올바르게 변환한다")
        void shouldConvertPriceInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.price()).isNotNull();
            assertThat(apiResponse.price().price()).isEqualTo(50000);
            assertThat(apiResponse.price().originalPrice()).isEqualTo(60000);
            assertThat(apiResponse.price().normalPrice()).isEqualTo(55000);
            assertThat(apiResponse.price().appPrice()).isEqualTo(48000);
            assertThat(apiResponse.price().discountRate()).isEqualTo(10);
            assertThat(apiResponse.price().appDiscountRate()).isEqualTo(15);
        }

        @Test
        @DisplayName("가격 정보가 null이면 기본값을 반환한다")
        void shouldReturnDefaultPriceInfoWhenNull() {
            // Given
            CrawledProductDetailResponse appResponse = createDetailResponseWithNullPrice();

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.price()).isNotNull();
            assertThat(apiResponse.price().price()).isZero();
            assertThat(apiResponse.price().originalPrice()).isZero();
        }

        @Test
        @DisplayName("이미지 정보를 올바르게 변환한다")
        void shouldConvertImagesInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.images()).isNotNull();
            assertThat(apiResponse.images().thumbnails()).hasSize(1);
            assertThat(apiResponse.images().descriptionImages()).hasSize(1);
            assertThat(apiResponse.images().totalCount()).isEqualTo(2);
            assertThat(apiResponse.images().uploadedCount()).isEqualTo(1);

            // 썸네일 상세 검증
            var thumbnail = apiResponse.images().thumbnails().get(0);
            assertThat(thumbnail.originalUrl()).isEqualTo("http://example.com/thumb.jpg");
            assertThat(thumbnail.s3Url()).isEqualTo("http://s3.example.com/thumb.jpg");
            assertThat(thumbnail.status()).isEqualTo("UPLOADED");
            assertThat(thumbnail.displayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("이미지 정보가 null이면 빈 리스트를 반환한다")
        void shouldReturnEmptyImagesWhenNull() {
            // Given
            CrawledProductDetailResponse appResponse = createDetailResponseWithNullImages();

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.images()).isNotNull();
            assertThat(apiResponse.images().thumbnails()).isEmpty();
            assertThat(apiResponse.images().descriptionImages()).isEmpty();
            assertThat(apiResponse.images().totalCount()).isZero();
            assertThat(apiResponse.images().uploadedCount()).isZero();
        }

        @Test
        @DisplayName("카테고리 정보를 올바르게 변환한다")
        void shouldConvertCategoryInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.category()).isNotNull();
            assertThat(apiResponse.category().fullPath()).isEqualTo("패션 > 의류 > 티셔츠");
            assertThat(apiResponse.category().headerCategoryCode()).isEqualTo("001");
            assertThat(apiResponse.category().headerCategoryName()).isEqualTo("패션");
            assertThat(apiResponse.category().largeCategoryCode()).isEqualTo("001001");
            assertThat(apiResponse.category().largeCategoryName()).isEqualTo("의류");
            assertThat(apiResponse.category().mediumCategoryCode()).isEqualTo("001001001");
            assertThat(apiResponse.category().mediumCategoryName()).isEqualTo("티셔츠");
        }

        @Test
        @DisplayName("카테고리 정보가 null이면 null을 반환한다")
        void shouldReturnNullCategoryWhenNull() {
            // Given
            CrawledProductDetailResponse appResponse = createDetailResponseWithNullCategory();

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.category()).isNull();
        }

        @Test
        @DisplayName("배송 정보를 올바르게 변환한다")
        void shouldConvertShippingInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.shipping()).isNotNull();
            assertThat(apiResponse.shipping().shippingType()).isEqualTo("DOMESTIC");
            assertThat(apiResponse.shipping().shippingFee()).isEqualTo(3000);
            assertThat(apiResponse.shipping().shippingFeeType()).isEqualTo("FIXED");
            assertThat(apiResponse.shipping().averageDeliveryDays()).isEqualTo(3);
            assertThat(apiResponse.shipping().freeShipping()).isFalse();
        }

        @Test
        @DisplayName("배송 정보가 null이면 null을 반환한다")
        void shouldReturnNullShippingWhenNull() {
            // Given
            CrawledProductDetailResponse appResponse = createDetailResponseWithNullShipping();

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.shipping()).isNull();
        }

        @Test
        @DisplayName("옵션 정보를 올바르게 변환한다")
        void shouldConvertOptionsInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.options()).isNotNull();
            assertThat(apiResponse.options().options()).hasSize(1);
            assertThat(apiResponse.options().totalStock()).isEqualTo(100);
            assertThat(apiResponse.options().inStockCount()).isEqualTo(1);
            assertThat(apiResponse.options().soldOutCount()).isZero();
            assertThat(apiResponse.options().distinctColors()).containsExactly("블랙");
            assertThat(apiResponse.options().distinctSizes()).containsExactly("M");

            // 옵션 상세 검증
            var option = apiResponse.options().options().get(0);
            assertThat(option.optionNo()).isEqualTo(1L);
            assertThat(option.color()).isEqualTo("블랙");
            assertThat(option.size()).isEqualTo("M");
            assertThat(option.stock()).isEqualTo(100);
        }

        @Test
        @DisplayName("옵션 정보가 null이면 빈 옵션 정보를 반환한다")
        void shouldReturnEmptyOptionsWhenNull() {
            // Given
            CrawledProductDetailResponse appResponse = createDetailResponseWithNullOptions();

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.options()).isNotNull();
            assertThat(apiResponse.options().options()).isEmpty();
            assertThat(apiResponse.options().totalStock()).isZero();
            assertThat(apiResponse.options().inStockCount()).isZero();
            assertThat(apiResponse.options().soldOutCount()).isZero();
            assertThat(apiResponse.options().distinctColors()).isEmpty();
            assertThat(apiResponse.options().distinctSizes()).isEmpty();
        }

        @Test
        @DisplayName("크롤링 상태 정보를 올바르게 변환한다")
        void shouldConvertCrawlStatusInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.crawlStatus()).isNotNull();
            assertThat(apiResponse.crawlStatus().miniShopCrawledAt()).isEqualTo(now.toString());
            assertThat(apiResponse.crawlStatus().detailCrawledAt()).isEqualTo(now.toString());
            assertThat(apiResponse.crawlStatus().optionCrawledAt()).isNull();
            assertThat(apiResponse.crawlStatus().completedCount()).isEqualTo(2);
            assertThat(apiResponse.crawlStatus().pendingTypes()).containsExactly("OPTION");
        }

        @Test
        @DisplayName("크롤링 상태가 null이면 기본값을 반환한다")
        void shouldReturnDefaultCrawlStatusWhenNull() {
            // Given
            CrawledProductDetailResponse appResponse = createDetailResponseWithNullCrawlStatus();

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.crawlStatus()).isNotNull();
            assertThat(apiResponse.crawlStatus().miniShopCrawledAt()).isNull();
            assertThat(apiResponse.crawlStatus().detailCrawledAt()).isNull();
            assertThat(apiResponse.crawlStatus().optionCrawledAt()).isNull();
            assertThat(apiResponse.crawlStatus().completedCount()).isZero();
            assertThat(apiResponse.crawlStatus().pendingTypes()).isEmpty();
        }

        @Test
        @DisplayName("동기화 상태 정보를 올바르게 변환한다")
        void shouldConvertSyncStatusInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProductDetailResponse appResponse = createFullDetailResponse(now);

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.syncStatus()).isNotNull();
            assertThat(apiResponse.syncStatus().externalProductId()).isEqualTo(999L);
            assertThat(apiResponse.syncStatus().needsSync()).isTrue();
            assertThat(apiResponse.syncStatus().lastSyncedAt()).isEqualTo(now.toString());
            assertThat(apiResponse.syncStatus().canSync()).isTrue();
        }

        @Test
        @DisplayName("동기화 상태가 null이면 기본값을 반환한다")
        void shouldReturnDefaultSyncStatusWhenNull() {
            // Given
            CrawledProductDetailResponse appResponse = createDetailResponseWithNullSyncStatus();

            // When
            CrawledProductDetailApiResponse apiResponse = mapper.toDetailApiResponse(appResponse);

            // Then
            assertThat(apiResponse.syncStatus()).isNotNull();
            assertThat(apiResponse.syncStatus().externalProductId()).isNull();
            assertThat(apiResponse.syncStatus().needsSync()).isFalse();
            assertThat(apiResponse.syncStatus().lastSyncedAt()).isNull();
            assertThat(apiResponse.syncStatus().canSync()).isFalse();
        }
    }

    @Nested
    @DisplayName("toPageApiResponse()는")
    class ToPageApiResponse {

        @Test
        @DisplayName("PageResponse를 PageApiResponse로 변환한다")
        void shouldConvertPageResponse() {
            // Given
            Instant now = Instant.now();
            List<CrawledProductSummaryResponse> items =
                    List.of(
                            new CrawledProductSummaryResponse(
                                    1L, 100L, 12345L, "상품1", "브랜드1", 50000, 10, 2, "OPTION", true,
                                    null, null, true, 50, now, now),
                            new CrawledProductSummaryResponse(
                                    2L, 100L, 12346L, "상품2", "브랜드2", 30000, 5, 3, "", false, 888L,
                                    now, false, 0, now, now));
            PageResponse<CrawledProductSummaryResponse> appPageResponse =
                    new PageResponse<>(items, 0, 20, 50L, 3, true, false);

            // When
            PageApiResponse<CrawledProductSummaryApiResponse> apiPageResponse =
                    mapper.toPageApiResponse(appPageResponse);

            // Then
            assertThat(apiPageResponse.content()).hasSize(2);
            assertThat(apiPageResponse.page()).isZero();
            assertThat(apiPageResponse.size()).isEqualTo(20);
            assertThat(apiPageResponse.totalElements()).isEqualTo(50L);
            assertThat(apiPageResponse.totalPages()).isEqualTo(3);
            assertThat(apiPageResponse.last()).isFalse();

            // 첫 번째 항목 검증
            CrawledProductSummaryApiResponse first = apiPageResponse.content().get(0);
            assertThat(first.id()).isEqualTo(1L);
            assertThat(first.itemName()).isEqualTo("상품1");
        }

        @Test
        @DisplayName("빈 목록도 정상 변환한다")
        void shouldHandleEmptyList() {
            // Given
            PageResponse<CrawledProductSummaryResponse> emptyPageResponse =
                    new PageResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            // When
            PageApiResponse<CrawledProductSummaryApiResponse> apiPageResponse =
                    mapper.toPageApiResponse(emptyPageResponse);

            // Then
            assertThat(apiPageResponse.content()).isEmpty();
            assertThat(apiPageResponse.totalElements()).isZero();
            assertThat(apiPageResponse.totalPages()).isZero();
            assertThat(apiPageResponse.last()).isTrue();
        }
    }

    // ========== Test Data Factory Methods ==========

    private CrawledProductDetailResponse createFullDetailResponse(Instant now) {
        return new CrawledProductDetailResponse(
                1L,
                100L,
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                "SALE",
                "대한민국",
                "서울",
                true,
                new CrawledProductDetailResponse.PriceInfo(50000, 60000, 55000, 48000, 10, 15),
                new CrawledProductDetailResponse.ImagesInfo(
                        List.of(
                                new CrawledProductDetailResponse.ImageInfo(
                                        "http://example.com/thumb.jpg",
                                        "http://s3.example.com/thumb.jpg",
                                        "UPLOADED",
                                        1)),
                        List.of(
                                new CrawledProductDetailResponse.ImageInfo(
                                        "http://example.com/desc.jpg", null, "PENDING", 1)),
                        2,
                        1),
                new CrawledProductDetailResponse.CategoryInfo(
                        "패션 > 의류 > 티셔츠", "001", "패션", "001001", "의류", "001001001", "티셔츠"),
                new CrawledProductDetailResponse.ShippingInfoDto(
                        "DOMESTIC", 3000, "FIXED", 3, false),
                new CrawledProductDetailResponse.OptionsInfo(
                        List.of(new CrawledProductDetailResponse.OptionInfo(1L, "블랙", "M", 100)),
                        100,
                        1,
                        0,
                        List.of("블랙"),
                        List.of("M")),
                new CrawledProductDetailResponse.CrawlStatusInfo(
                        now, now, null, 2, List.of("OPTION")),
                new CrawledProductDetailResponse.SyncStatusInfo(999L, true, now, true),
                "<p>상품 설명</p>",
                now,
                now);
    }

    private CrawledProductDetailResponse createDetailResponseWithNullPrice() {
        return new CrawledProductDetailResponse(
                1L, 100L, 12345L, "상품", "브랜드", "SALE", null, null, false, null, null, null, null,
                null, null, null, null, null, null);
    }

    private CrawledProductDetailResponse createDetailResponseWithNullImages() {
        return new CrawledProductDetailResponse(
                1L,
                100L,
                12345L,
                "상품",
                "브랜드",
                "SALE",
                null,
                null,
                false,
                new CrawledProductDetailResponse.PriceInfo(0, 0, 0, 0, 0, 0),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private CrawledProductDetailResponse createDetailResponseWithNullCategory() {
        return new CrawledProductDetailResponse(
                1L,
                100L,
                12345L,
                "상품",
                "브랜드",
                "SALE",
                null,
                null,
                false,
                new CrawledProductDetailResponse.PriceInfo(0, 0, 0, 0, 0, 0),
                new CrawledProductDetailResponse.ImagesInfo(List.of(), List.of(), 0, 0),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private CrawledProductDetailResponse createDetailResponseWithNullShipping() {
        return new CrawledProductDetailResponse(
                1L,
                100L,
                12345L,
                "상품",
                "브랜드",
                "SALE",
                null,
                null,
                false,
                new CrawledProductDetailResponse.PriceInfo(0, 0, 0, 0, 0, 0),
                new CrawledProductDetailResponse.ImagesInfo(List.of(), List.of(), 0, 0),
                new CrawledProductDetailResponse.CategoryInfo(
                        null, null, null, null, null, null, null),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private CrawledProductDetailResponse createDetailResponseWithNullOptions() {
        return new CrawledProductDetailResponse(
                1L,
                100L,
                12345L,
                "상품",
                "브랜드",
                "SALE",
                null,
                null,
                false,
                new CrawledProductDetailResponse.PriceInfo(0, 0, 0, 0, 0, 0),
                new CrawledProductDetailResponse.ImagesInfo(List.of(), List.of(), 0, 0),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private CrawledProductDetailResponse createDetailResponseWithNullCrawlStatus() {
        return new CrawledProductDetailResponse(
                1L,
                100L,
                12345L,
                "상품",
                "브랜드",
                "SALE",
                null,
                null,
                false,
                new CrawledProductDetailResponse.PriceInfo(0, 0, 0, 0, 0, 0),
                new CrawledProductDetailResponse.ImagesInfo(List.of(), List.of(), 0, 0),
                null,
                null,
                new CrawledProductDetailResponse.OptionsInfo(
                        List.of(), 0, 0, 0, List.of(), List.of()),
                null,
                null,
                null,
                null,
                null);
    }

    private CrawledProductDetailResponse createDetailResponseWithNullSyncStatus() {
        return new CrawledProductDetailResponse(
                1L,
                100L,
                12345L,
                "상품",
                "브랜드",
                "SALE",
                null,
                null,
                false,
                new CrawledProductDetailResponse.PriceInfo(0, 0, 0, 0, 0, 0),
                new CrawledProductDetailResponse.ImagesInfo(List.of(), List.of(), 0, 0),
                null,
                null,
                new CrawledProductDetailResponse.OptionsInfo(
                        List.of(), 0, 0, 0, List.of(), List.of()),
                new CrawledProductDetailResponse.CrawlStatusInfo(null, null, null, 0, List.of()),
                null,
                null,
                null,
                null);
    }
}
