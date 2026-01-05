package com.ryuqq.crawlinghub.domain.task.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlEndpoint Value Object 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
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
