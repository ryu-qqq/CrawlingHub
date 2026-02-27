package com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
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
                    "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=LIKEASTAR&sort=LATEST&f=us:NEW,lwp:Y&pageNo=1";
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
