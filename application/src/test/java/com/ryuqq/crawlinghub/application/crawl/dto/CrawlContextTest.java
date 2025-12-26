package com.ryuqq.crawlinghub.application.crawl.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@DisplayName("CrawlContext 단위 테스트")
class CrawlContextTest {

    @Nested
    @DisplayName("buildSearchEndpoint() 메서드는")
    class BuildSearchEndpoint {

        @Test
        @DisplayName("nid와 mustitUid가 있으면 쿼리 파라미터를 추가한다")
        void shouldAppendQueryParamsWhenHasSearchCookies() {
            // Given
            String baseEndpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=LIKEASTAR&sort=POPULAR2&f=us:NEW,lwp:Y&pageNo=1";
            String nid = "3cb41b66-b356-4094-9df3-adf8deab19f1";
            String mustitUid = "1764141487281.758544";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            baseEndpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            nid,
                            mustitUid);

            // When
            String result = context.buildSearchEndpoint();

            // Then
            assertThat(result).contains("nid=" + nid);
            assertThat(result).contains("uid=" + mustitUid);
            assertThat(result).contains("adId=" + mustitUid);
            assertThat(result).contains("beforeItemType=Normal");
            assertThat(result).startsWith(baseEndpoint + "&");
        }

        @Test
        @DisplayName("기존 쿼리 파라미터가 없어도 ?로 시작하여 파라미터를 추가한다")
        void shouldAppendQueryParamsWithQuestionMarkWhenNoExistingParams() {
            // Given
            String baseEndpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items";
            String nid = "test-nid-uuid";
            String mustitUid = "1234567890.12345";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            baseEndpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            nid,
                            mustitUid);

            // When
            String result = context.buildSearchEndpoint();

            // Then
            assertThat(result).startsWith(baseEndpoint + "?");
            assertThat(result).contains("nid=" + nid);
            assertThat(result).contains("uid=" + mustitUid);
            assertThat(result).contains("adId=" + mustitUid);
        }

        @Test
        @DisplayName("nid가 null이면 기본 endpoint를 반환한다")
        void shouldReturnBaseEndpointWhenNidIsNull() {
            // Given
            String baseEndpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=TEST";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            baseEndpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            null,
                            "1234567890.12345");

            // When
            String result = context.buildSearchEndpoint();

            // Then
            assertThat(result).isEqualTo(baseEndpoint);
        }

        @Test
        @DisplayName("mustitUid가 blank이면 기본 endpoint를 반환한다")
        void shouldReturnBaseEndpointWhenMusitUidIsBlank() {
            // Given
            String baseEndpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=TEST";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            baseEndpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            "nid-value",
                            "   ");

            // When
            String result = context.buildSearchEndpoint();

            // Then
            assertThat(result).isEqualTo(baseEndpoint);
        }
    }

    @Nested
    @DisplayName("hasSearchCookies() 메서드는")
    class HasSearchCookies {

        @Test
        @DisplayName("nid와 mustitUid가 모두 있으면 true를 반환한다")
        void shouldReturnTrueWhenBothValuesPresent() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            "Mozilla/5.0",
                            "token",
                            "nid-value",
                            "mustit-uid-value");

            // When & Then
            assertThat(context.hasSearchCookies()).isTrue();
        }

        @Test
        @DisplayName("nid가 null이면 false를 반환한다")
        void shouldReturnFalseWhenNidIsNull() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            "Mozilla/5.0",
                            "token",
                            null,
                            "mustit-uid-value");

            // When & Then
            assertThat(context.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("mustitUid가 blank이면 false를 반환한다")
        void shouldReturnFalseWhenMusitUidIsBlank() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            "Mozilla/5.0",
                            "token",
                            "nid-value",
                            "");

            // When & Then
            assertThat(context.hasSearchCookies()).isFalse();
        }
    }

    @Nested
    @DisplayName("buildHeaders() 메서드는")
    class BuildHeaders {

        @Test
        @DisplayName("User-Agent, Authorization, Cookie 헤더를 포함한다")
        void shouldIncludeAllRequiredHeaders() {
            // Given
            String nid = "test-nid";
            String mustitUid = "test-mustit-uid";
            String userAgent = "Mozilla/5.0";
            String token = "test-token";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            userAgent,
                            token,
                            nid,
                            mustitUid);

            // When
            var headers = context.buildHeaders();

            // Then
            assertThat(headers).containsEntry("User-Agent", userAgent);
            assertThat(headers).containsEntry("Authorization", "Bearer " + token);
            assertThat(headers.get("Cookie")).contains("nid=" + nid);
            assertThat(headers.get("Cookie")).contains("mustit_uid=" + mustitUid);
        }
    }
}
