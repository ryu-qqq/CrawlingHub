package com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@DisplayName("CrawlContextMapper 단위 테스트")
class CrawlContextMapperTest {

    private final CrawlContextMapper mapper = new CrawlContextMapper();

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
            Map<String, String> headers = mapper.buildHeaders(context);

            // Then
            assertThat(headers).containsEntry("User-Agent", userAgent);
            assertThat(headers).containsEntry("Authorization", "Bearer " + token);
            assertThat(headers.get("Cookie")).contains("nid=" + nid);
            assertThat(headers.get("Cookie")).contains("mustit_uid=" + mustitUid);
        }

        @Test
        @DisplayName("userAgentValue가 null이면 User-Agent 헤더를 포함하지 않는다")
        void shouldNotIncludeUserAgentWhenNull() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            null,
                            "token",
                            null,
                            null);

            // When
            Map<String, String> headers = mapper.buildHeaders(context);

            // Then
            assertThat(headers).doesNotContainKey("User-Agent");
        }

        @Test
        @DisplayName("sessionToken이 blank이면 Authorization 헤더를 포함하지 않는다")
        void shouldNotIncludeAuthWhenBlank() {
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
                            "   ",
                            null,
                            null);

            // When
            Map<String, String> headers = mapper.buildHeaders(context);

            // Then
            assertThat(headers).doesNotContainKey("Authorization");
        }
    }

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
            String result = mapper.buildSearchEndpoint(context);

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
            String result = mapper.buildSearchEndpoint(context);

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
            String result = mapper.buildSearchEndpoint(context);

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
            String result = mapper.buildSearchEndpoint(context);

            // Then
            assertThat(result).isEqualTo(baseEndpoint);
        }
    }
}
