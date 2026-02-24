package com.ryuqq.crawlinghub.domain.task.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlEndpoint Value Object 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CrawlEndpoint VO 테스트")
class CrawlEndpointTest {

    private static final String MUSTIT_BASE_URL = "https://m.web.mustit.co.kr";

    @Nested
    @DisplayName("생성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("baseUrl이 null이면 예외 발생")
        void shouldThrowExceptionWhenBaseUrlIsNull() {
            assertThatThrownBy(() -> new CrawlEndpoint(null, "/path", Collections.emptyMap()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("baseUrl");
        }

        @Test
        @DisplayName("path가 null이면 예외 발생")
        void shouldThrowExceptionWhenPathIsNull() {
            assertThatThrownBy(
                            () -> new CrawlEndpoint(MUSTIT_BASE_URL, null, Collections.emptyMap()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("path");
        }

        @Test
        @DisplayName("queryParams가 null이면 빈 Map으로 처리")
        void shouldCreateWithEmptyQueryParamsWhenNull() {
            CrawlEndpoint endpoint = new CrawlEndpoint(MUSTIT_BASE_URL, "/path", null);
            assertThat(endpoint.queryParams()).isEmpty();
        }
    }

    @Nested
    @DisplayName("정적 팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("forMiniShopList로 생성")
        void shouldCreateForMiniShopList() {
            // given
            String mustItSellerName = "test-seller";
            int page = 1;
            int pageSize = 20;

            // when
            CrawlEndpoint endpoint =
                    CrawlEndpoint.forMiniShopList(mustItSellerName, page, pageSize);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo(MUSTIT_BASE_URL);
            assertThat(endpoint.path())
                    .isEqualTo("/mustit-api/facade-api/v1/searchmini-shop-search");
            assertThat(endpoint.queryParams()).containsEntry("sellerId", "test-seller");
            assertThat(endpoint.queryParams()).containsEntry("pageNo", "1");
            assertThat(endpoint.queryParams()).containsEntry("pageSize", "20");
            assertThat(endpoint.queryParams()).containsEntry("order", "LATEST");
        }

        @Test
        @DisplayName("forProductDetail로 생성")
        void shouldCreateForProductDetail() {
            // given
            Long itemNo = 99999L;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forProductDetail(itemNo);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo(MUSTIT_BASE_URL);
            assertThat(endpoint.path())
                    .isEqualTo("/mustit-api/facade-api/v1/item/99999/detail/top");
            assertThat(endpoint.queryParams()).isEmpty();
        }

        @Test
        @DisplayName("forProductOption로 생성")
        void shouldCreateForProductOption() {
            // given
            Long itemNo = 99999L;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forProductOption(itemNo);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo(MUSTIT_BASE_URL);
            assertThat(endpoint.path())
                    .isEqualTo("/mustit-api/legacy-api/v1/auction_products/99999/options");
            assertThat(endpoint.queryParams()).isEmpty();
        }

        @Test
        @DisplayName("forSearchItems로 검색 API 엔드포인트 생성")
        void shouldCreateForSearchItems() {
            // given
            String keyword = "test-seller";
            int pageNo = 1;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forSearchItems(keyword, pageNo);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo(MUSTIT_BASE_URL);
            assertThat(endpoint.path()).isEqualTo("/mustit-api/facade-api/v1/search/items");
            assertThat(endpoint.queryParams()).containsEntry("keyword", "test-seller");
            assertThat(endpoint.queryParams()).containsEntry("sort", "POPULAR2");
            assertThat(endpoint.queryParams()).containsEntry("f", "us:NEW,lwp:Y");
            assertThat(endpoint.queryParams()).containsEntry("pageNo", "1");
        }

        @Test
        @DisplayName("forSearchItems에 null keyword 전달 시 예외 발생")
        void shouldThrowExceptionWhenSearchItemsKeywordIsNull() {
            assertThatThrownBy(() -> CrawlEndpoint.forSearchItems(null, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("keyword");
        }

        @Test
        @DisplayName("forSearchItems에 빈 keyword 전달 시 예외 발생")
        void shouldThrowExceptionWhenSearchItemsKeywordIsBlank() {
            assertThatThrownBy(() -> CrawlEndpoint.forSearchItems("   ", 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("keyword");
        }

        @Test
        @DisplayName("forMiniShopList에 null sellerName 전달 시 예외 발생")
        void shouldThrowExceptionWhenMiniShopSellerNameIsNull() {
            assertThatThrownBy(() -> CrawlEndpoint.forMiniShopList(null, 1, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mustItSellerName");
        }

        @Test
        @DisplayName("forMiniShopList에 빈 sellerName 전달 시 예외 발생")
        void shouldThrowExceptionWhenMiniShopSellerNameIsBlank() {
            assertThatThrownBy(() -> CrawlEndpoint.forMiniShopList("  ", 1, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mustItSellerName");
        }

        @Test
        @DisplayName("forSearchApi에 null URL 전달 시 예외 발생")
        void shouldThrowExceptionWhenSearchApiUrlIsNull() {
            assertThatThrownBy(() -> CrawlEndpoint.forSearchApi(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("URL");
        }

        @Test
        @DisplayName("forSearchApi에 빈 URL 전달 시 예외 발생")
        void shouldThrowExceptionWhenSearchApiUrlIsBlank() {
            assertThatThrownBy(() -> CrawlEndpoint.forSearchApi("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("URL");
        }

        @Test
        @DisplayName("forSearchApi로 쿼리파라미터가 있는 URL 파싱")
        void shouldParseSearchApiUrlWithQueryParams() {
            String fullUrl =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=seller&sort=POPULAR2&pageNo=2";
            CrawlEndpoint endpoint = CrawlEndpoint.forSearchApi(fullUrl);

            assertThat(endpoint.baseUrl()).isEqualTo("https://m.web.mustit.co.kr");
            assertThat(endpoint.path()).isEqualTo("/mustit-api/facade-api/v1/search/items");
            assertThat(endpoint.queryParams()).containsEntry("keyword", "seller");
            assertThat(endpoint.queryParams()).containsEntry("sort", "POPULAR2");
            assertThat(endpoint.queryParams()).containsEntry("pageNo", "2");
        }

        @Test
        @DisplayName("forSearchApi로 쿼리파라미터가 없는 URL 파싱")
        void shouldParseSearchApiUrlWithoutQueryParams() {
            String fullUrl = "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items";
            CrawlEndpoint endpoint = CrawlEndpoint.forSearchApi(fullUrl);

            assertThat(endpoint.baseUrl()).isEqualTo("https://m.web.mustit.co.kr");
            assertThat(endpoint.path()).isEqualTo("/mustit-api/facade-api/v1/search/items");
            assertThat(endpoint.queryParams()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toFullUrl() 테스트")
    class ToFullUrlTest {

        @Test
        @DisplayName("queryParams가 있을 때 전체 URL 생성")
        void shouldBuildFullUrlWithQueryParams() {
            CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopList("test-seller", 1, 20);
            String fullUrl = endpoint.toFullUrl();

            assertThat(fullUrl)
                    .startsWith(
                            MUSTIT_BASE_URL + "/mustit-api/facade-api/v1/searchmini-shop-search?");
            assertThat(fullUrl).contains("sellerId=test-seller");
            assertThat(fullUrl).contains("pageNo=1");
            assertThat(fullUrl).contains("pageSize=20");
            assertThat(fullUrl).contains("order=LATEST");
        }

        @Test
        @DisplayName("queryParams가 없을 때 전체 URL 생성")
        void shouldBuildFullUrlWithoutQueryParams() {
            CrawlEndpoint endpoint = CrawlEndpoint.forProductDetail(99999L);
            String fullUrl = endpoint.toFullUrl();

            assertThat(fullUrl)
                    .isEqualTo(MUSTIT_BASE_URL + "/mustit-api/facade-api/v1/item/99999/detail/top");
        }
    }

    @Nested
    @DisplayName("toQueryParamsJson() 테스트")
    class ToQueryParamsJsonTest {

        @Test
        @DisplayName("queryParams가 있으면 JSON 문자열을 반환한다")
        void returnsJsonStringWhenQueryParamsExist() {
            CrawlEndpoint endpoint = CrawlEndpoint.forProductDetail(99999L);
            // 빈 queryParams인 경우 null 반환
            assertThat(endpoint.toQueryParamsJson()).isNull();
        }

        @Test
        @DisplayName("queryParams가 있으면 JSON 형식 문자열을 반환한다")
        void returnsJsonFormattedString() {
            CrawlEndpoint endpoint =
                    new CrawlEndpoint(MUSTIT_BASE_URL, "/path", Map.of("key", "value"));
            String json = endpoint.toQueryParamsJson();
            assertThat(json).isNotNull();
            assertThat(json).startsWith("{");
            assertThat(json).endsWith("}");
            assertThat(json).contains("\"key\":\"value\"");
        }
    }

    @Nested
    @DisplayName("getMustItSellerName() 테스트")
    class GetMustItSellerNameTest {

        @Test
        @DisplayName("MINI_SHOP 타입에서 sellerId를 셀러명으로 반환한다")
        void returnsSellerId() {
            CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopList("my-seller", 1, 20);
            assertThat(endpoint.getMustItSellerName()).isEqualTo("my-seller");
        }

        @Test
        @DisplayName("SEARCH 타입에서 keyword를 셀러명으로 반환한다")
        void returnsKeyword() {
            CrawlEndpoint endpoint = CrawlEndpoint.forSearchItems("search-seller", 1);
            assertThat(endpoint.getMustItSellerName()).isEqualTo("search-seller");
        }

        @Test
        @DisplayName("sellerId도 keyword도 없으면 null을 반환한다")
        void returnsNullWhenNoSellerParams() {
            CrawlEndpoint endpoint = CrawlEndpoint.forProductDetail(99999L);
            assertThat(endpoint.getMustItSellerName()).isNull();
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("queryParams는 불변")
        void shouldHaveImmutableQueryParams() {
            Map<String, String> params = new HashMap<>();
            params.put("key", "value");
            CrawlEndpoint endpoint = new CrawlEndpoint(MUSTIT_BASE_URL, "/path", params);

            Map<String, String> returnedParams = endpoint.queryParams();

            assertThatThrownBy(() -> returnedParams.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
