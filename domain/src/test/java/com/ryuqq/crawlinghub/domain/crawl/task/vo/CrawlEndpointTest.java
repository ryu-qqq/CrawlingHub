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
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlEndpoint VO 테스트")
class CrawlEndpointTest {

    @Nested
    @DisplayName("생성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("baseUrl이 null이면 예외 발생")
        void shouldThrowExceptionWhenBaseUrlIsNull() {
            // given & when & then
            assertThatThrownBy(() -> new CrawlEndpoint(null, "/path", Collections.emptyMap()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("baseUrl");
        }

        @Test
        @DisplayName("path가 null이면 예외 발생")
        void shouldThrowExceptionWhenPathIsNull() {
            // given & when & then
            assertThatThrownBy(() -> new CrawlEndpoint("https://api.mustit.co.kr", null, Collections.emptyMap()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("path");
        }

        @Test
        @DisplayName("queryParams가 null이면 빈 Map으로 처리")
        void shouldCreateWithEmptyQueryParamsWhenNull() {
            // given & when
            CrawlEndpoint endpoint = new CrawlEndpoint("https://api.mustit.co.kr", "/path", null);

            // then
            assertThat(endpoint.queryParams()).isEmpty();
        }

        @Test
        @DisplayName("빈 queryParams로 생성 가능")
        void shouldCreateWithEmptyQueryParams() {
            // given & when
            CrawlEndpoint endpoint = new CrawlEndpoint("https://api.mustit.co.kr", "/path", Collections.emptyMap());

            // then
            assertThat(endpoint.queryParams()).isEmpty();
        }
    }

    @Nested
    @DisplayName("정적 팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("forMiniShopMeta로 생성")
        void shouldCreateForMiniShopMeta() {
            // given
            Long sellerId = 12345L;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopMeta(sellerId);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo("https://api.mustit.co.kr");
            assertThat(endpoint.path()).contains("minishop");
            assertThat(endpoint.path()).contains(String.valueOf(sellerId));
        }

        @Test
        @DisplayName("forMiniShopList로 생성")
        void shouldCreateForMiniShopList() {
            // given
            Long sellerId = 12345L;
            int page = 1;
            int size = 20;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopList(sellerId, page, size);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo("https://api.mustit.co.kr");
            assertThat(endpoint.queryParams()).containsEntry("page", String.valueOf(page));
            assertThat(endpoint.queryParams()).containsEntry("size", String.valueOf(size));
        }

        @Test
        @DisplayName("forProductDetail로 생성")
        void shouldCreateForProductDetail() {
            // given
            Long productId = 99999L;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forProductDetail(productId);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo("https://api.mustit.co.kr");
            assertThat(endpoint.path()).contains("product");
            assertThat(endpoint.path()).contains(String.valueOf(productId));
        }

        @Test
        @DisplayName("forProductOption로 생성")
        void shouldCreateForProductOption() {
            // given
            Long productId = 99999L;

            // when
            CrawlEndpoint endpoint = CrawlEndpoint.forProductOption(productId);

            // then
            assertThat(endpoint.baseUrl()).isEqualTo("https://api.mustit.co.kr");
            assertThat(endpoint.path()).contains("option");
        }
    }

    @Nested
    @DisplayName("toFullUrl() 테스트")
    class ToFullUrlTest {

        @Test
        @DisplayName("queryParams가 있을 때 전체 URL 생성")
        void shouldBuildFullUrlWithQueryParams() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("page", "1");
            params.put("size", "20");
            CrawlEndpoint endpoint = new CrawlEndpoint("https://api.mustit.co.kr", "/products", params);

            // when
            String fullUrl = endpoint.toFullUrl();

            // then
            assertThat(fullUrl).startsWith("https://api.mustit.co.kr/products?");
            assertThat(fullUrl).contains("page=1");
            assertThat(fullUrl).contains("size=20");
        }

        @Test
        @DisplayName("queryParams가 없을 때 전체 URL 생성")
        void shouldBuildFullUrlWithoutQueryParams() {
            // given
            CrawlEndpoint endpoint = new CrawlEndpoint("https://api.mustit.co.kr", "/products", Collections.emptyMap());

            // when
            String fullUrl = endpoint.toFullUrl();

            // then
            assertThat(fullUrl).isEqualTo("https://api.mustit.co.kr/products");
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("queryParams는 불변")
        void shouldHaveImmutableQueryParams() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("key", "value");
            CrawlEndpoint endpoint = new CrawlEndpoint("https://api.mustit.co.kr", "/path", params);

            // when
            Map<String, String> returnedParams = endpoint.queryParams();

            // then
            assertThatThrownBy(() -> returnedParams.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
