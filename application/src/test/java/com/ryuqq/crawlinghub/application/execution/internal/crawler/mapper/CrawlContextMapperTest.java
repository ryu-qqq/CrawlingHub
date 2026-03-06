package com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlContextMapper 단위 테스트")
class CrawlContextMapperTest {

    private final CrawlContextMapper mapper = new CrawlContextMapper();

    @Nested
    @DisplayName("toCrawlContext(CrawlTask, BorrowedUserAgent) 메서드는")
    class ToCrawlContextWithBorrowedAgent {

        @Test
        @DisplayName("BorrowedUserAgent와 CrawlTask로 CrawlContext 생성")
        void shouldCreateCrawlContextFromBorrowedAgent() {
            // Given
            CrawlTask crawlTask = CrawlTaskFixture.aPublishedTask();
            BorrowedUserAgent agent =
                    new BorrowedUserAgent(
                            1L, "Mozilla/5.0", "session-token", "nid-value", "mustit-uid", 0);

            // When
            CrawlContext result = mapper.toCrawlContext(crawlTask, agent);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userAgentId()).isEqualTo(1L);
            assertThat(result.userAgentValue()).isEqualTo("Mozilla/5.0");
            assertThat(result.sessionToken()).isEqualTo("session-token");
            assertThat(result.nid()).isEqualTo("nid-value");
            assertThat(result.mustitUid()).isEqualTo("mustit-uid");
            assertThat(result.crawlTaskId()).isEqualTo(crawlTask.getIdValue());
        }

        @Test
        @DisplayName("sessionToken이 null인 BorrowedUserAgent로 CrawlContext 생성")
        void shouldCreateCrawlContextWithNullSessionToken() {
            // Given
            CrawlTask crawlTask = CrawlTaskFixture.aPublishedTask();
            BorrowedUserAgent agent = new BorrowedUserAgent(2L, "Mozilla/5.0", null, null, null, 0);

            // When
            CrawlContext result = mapper.toCrawlContext(crawlTask, agent);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.sessionToken()).isNull();
            assertThat(result.nid()).isNull();
        }
    }

    @Nested
    @DisplayName("toCrawlContext(CrawlTask, CachedUserAgent) 메서드는 (deprecated)")
    class ToCrawlContextWithCachedAgent {

        @Test
        @DisplayName("CachedUserAgent와 CrawlTask로 CrawlContext 생성")
        void shouldCreateCrawlContextFromCachedAgent() {
            // Given
            CrawlTask crawlTask = CrawlTaskFixture.aPublishedTask();
            // CachedUserAgent 파라미터 순서: id, value, sessionToken, nid, mustitUid, sessionExpiresAt,
            // remainingTokens, maxTokens, windowStart, windowEnd, healthScore, status,
            // suspendedAt, borrowedAt, cooldownUntil, consecutiveRateLimits
            CachedUserAgent cachedAgent =
                    new CachedUserAgent(
                            1L,
                            "Mozilla/5.0",
                            "cached-session-token",
                            null,
                            "cached-mustit-uid",
                            null,
                            80,
                            80,
                            null,
                            null,
                            90,
                            UserAgentStatus.IDLE,
                            null,
                            null,
                            null,
                            0);

            // When
            CrawlContext result = mapper.toCrawlContext(crawlTask, cachedAgent);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userAgentId()).isEqualTo(1L);
            assertThat(result.userAgentValue()).isEqualTo("Mozilla/5.0");
            assertThat(result.sessionToken()).isEqualTo("cached-session-token");
            assertThat(result.crawlTaskId()).isEqualTo(crawlTask.getIdValue());
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
            Map<String, String> headers = mapper.buildHeaders(context);

            // Then
            assertThat(headers).containsEntry("User-Agent", userAgent);
            assertThat(headers).containsEntry("Authorization", "Bearer " + token);
            assertThat(headers.get("Cookie")).contains("nid=" + nid);
            assertThat(headers.get("Cookie")).contains("mustit_uid=" + mustitUid);
            assertThat(headers).containsEntry("X-Route-Token", "Next-Route-Token");
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

        @Test
        @DisplayName("SEARCH 타입이 아니면 X-Route-Token 헤더를 포함하지 않는다")
        void shouldNotIncludeRouteTokenWhenNotSearchType() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.DETAIL,
                            "endpoint",
                            1L,
                            "Mozilla/5.0",
                            "token",
                            null,
                            null);

            // When
            Map<String, String> headers = mapper.buildHeaders(context);

            // Then
            assertThat(headers).doesNotContainKey("X-Route-Token");
        }
    }

    @Nested
    @DisplayName("buildSearchEndpoint() 메서드는")
    class BuildSearchEndpoint {

        private static final String BFF_PREFIX =
                "https://m.web.mustit.co.kr/v2/api/facade/searchItems?keyword=&sort=POPULAR2&nextApiUrl=";

        @Test
        @DisplayName("v1 URL을 BFF 포맷으로 래핑한다")
        void shouldWrapV1UrlIntoBffFormat() {
            // Given
            String v1Endpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=LIKEASTAR&sort=LATEST&pageNo=1";
            String nid = "3cb41b66-b356-4094-9df3-adf8deab19f1";
            String mustitUid = "1764141487281.758544";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            v1Endpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            nid,
                            mustitUid);

            // When
            String result = mapper.buildSearchEndpoint(context);

            // Then
            assertThat(result).startsWith(BFF_PREFIX);
            String encodedPath = result.substring(BFF_PREFIX.length());
            String decodedPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
            assertThat(decodedPath)
                    .isEqualTo("/v1/search/items?keyword=LIKEASTAR&sort=LATEST&pageNo=1");
        }

        @Test
        @DisplayName("쿼리 파라미터 없는 v1 URL도 BFF 포맷으로 래핑한다")
        void shouldWrapV1UrlWithoutQueryParamsIntoBffFormat() {
            // Given
            String v1Endpoint = "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items";
            String nid = "test-nid-uuid";
            String mustitUid = "1234567890.12345";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            v1Endpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            nid,
                            mustitUid);

            // When
            String result = mapper.buildSearchEndpoint(context);

            // Then
            assertThat(result).startsWith(BFF_PREFIX);
            String encodedPath = result.substring(BFF_PREFIX.length());
            String decodedPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
            assertThat(decodedPath).isEqualTo("/v1/search/items");
        }

        @Test
        @DisplayName("nextApiUrl 내 특수문자가 URL 인코딩된다")
        void shouldUrlEncodeNextApiUrl() {
            // Given
            String nextPageEndpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?f=us:NEW,lwp:Y&sort=LATEST&beforeItemType=Normal&keyword=ccapsule1&previousIdx=0%2C0&pageNo=2";
            String nid = "3cb41b66-b356-4094-9df3-adf8deab19f1";
            String mustitUid = "1764141487281.758544";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            nextPageEndpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            nid,
                            mustitUid);

            // When
            String result = mapper.buildSearchEndpoint(context);

            // Then
            assertThat(result).startsWith(BFF_PREFIX);
            // nextApiUrl 부분이 인코딩되어 있으므로 & 등이 원본 URL에 직접 나오지 않음
            String encodedPath = result.substring(BFF_PREFIX.length());
            assertThat(encodedPath).doesNotContain("&sort=LATEST");
            // 디코딩하면 원래 v1 상대 경로가 복원됨
            String decodedPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
            assertThat(decodedPath).startsWith("/v1/search/items?");
            assertThat(decodedPath).contains("keyword=ccapsule1");
            assertThat(decodedPath).contains("pageNo=2");
        }

        @Test
        @DisplayName("쿠키가 없어도 BFF URL을 반환한다 (nid null)")
        void shouldReturnBffUrlWhenNidIsNull() {
            // Given
            String v1Endpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=TEST";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            v1Endpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            null,
                            "1234567890.12345");

            // When
            String result = mapper.buildSearchEndpoint(context);

            // Then
            assertThat(result).startsWith(BFF_PREFIX);
            String encodedPath = result.substring(BFF_PREFIX.length());
            String decodedPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
            assertThat(decodedPath).isEqualTo("/v1/search/items?keyword=TEST");
        }

        @Test
        @DisplayName("쿠키가 없어도 BFF URL을 반환한다 (mustitUid blank)")
        void shouldReturnBffUrlWhenMusitUidIsBlank() {
            // Given
            String v1Endpoint =
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=TEST";

            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            v1Endpoint,
                            1L,
                            "Mozilla/5.0",
                            "token-123",
                            "nid-value",
                            "   ");

            // When
            String result = mapper.buildSearchEndpoint(context);

            // Then
            assertThat(result).startsWith(BFF_PREFIX);
            String encodedPath = result.substring(BFF_PREFIX.length());
            String decodedPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
            assertThat(decodedPath).isEqualTo("/v1/search/items?keyword=TEST");
        }
    }
}
