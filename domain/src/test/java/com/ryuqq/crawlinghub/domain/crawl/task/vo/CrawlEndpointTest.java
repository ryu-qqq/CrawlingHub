package com.ryuqq.crawlinghub.domain.crawl.task.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            assertThatThrownBy(() -> new CrawlEndpoint(MUSTIT_BASE_URL, null, Collections.emptyMap()))
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
            Long sellerId = 12345L;
            int page = 1;
            int pageSize = 20;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopList(sellerId, page, pageSize);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo(MUSTIT_BASE_URL);
            assertThat(endpoint.path()).isEqualTo("/mustit-api/facade-api/v1/searchmini-shop-search");
            assertThat(endpoint.queryParams()).containsEntry("sellerId", "12345");
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
            assertThat(endpoint.path()).isEqualTo("/mustit-api/facade-api/v1/item/99999/detail/top");
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
            assertThat(endpoint.path()).isEqualTo("/mustit-api/legacy-api/v1/auction_products/99999/options");
            assertThat(endpoint.queryParams()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toFullUrl() 테스트")
    class ToFullUrlTest {

        @Test
        @DisplayName("queryParams가 있을 때 전체 URL 생성")
        void shouldBuildFullUrlWithQueryParams() {
            CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopList(12345L, 1, 20);
            String fullUrl = endpoint.toFullUrl();

            assertThat(fullUrl).startsWith(MUSTIT_BASE_URL + "/mustit-api/facade-api/v1/searchmini-shop-search?");
            assertThat(fullUrl).contains("sellerId=12345");
            assertThat(fullUrl).contains("pageNo=1");
            assertThat(fullUrl).contains("pageSize=20");
            assertThat(fullUrl).contains("order=LATEST");
        }

        @Test
        @DisplayName("queryParams가 없을 때 전체 URL 생성")
        void shouldBuildFullUrlWithoutQueryParams() {
            CrawlEndpoint endpoint = CrawlEndpoint.forProductDetail(99999L);
            String fullUrl = endpoint.toFullUrl();

            assertThat(fullUrl).isEqualTo(MUSTIT_BASE_URL + "/mustit-api/facade-api/v1/item/99999/detail/top");
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
