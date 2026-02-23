package com.ryuqq.crawlinghub.application.crawl.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.SearchCrawler;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper.CrawlContextMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper.CrawlResultMapper;
import com.ryuqq.crawlinghub.application.execution.port.out.client.HttpClient;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SearchCrawler 단위 테스트
 *
 * <p>Search API 크롤러 테스트 (MustIt 무한스크롤)
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCrawler 테스트")
class SearchCrawlerTest {

    @Mock private HttpClient httpClient;
    @Mock private CrawlContextMapper crawlContextMapper;
    @Mock private CrawlResultMapper crawlResultMapper;

    private SearchCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new SearchCrawler(httpClient, crawlContextMapper, crawlResultMapper);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] SEARCH 타입 반환")
        void shouldReturnSearchType() {
            // When
            CrawlTaskType result = crawler.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.SEARCH);
        }
    }

    @Nested
    @DisplayName("supports() 테스트")
    class Supports {

        @Test
        @DisplayName("[성공] SEARCH 타입 지원")
        void shouldSupportSearchType() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.SEARCH)).isTrue();
        }

        @Test
        @DisplayName("[성공] 다른 타입 미지원")
        void shouldNotSupportOtherTypes() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.DETAIL)).isFalse();
            assertThat(crawler.supports(CrawlTaskType.MINI_SHOP)).isFalse();
        }
    }

    @Nested
    @DisplayName("crawl() 테스트")
    class Crawl {

        @Test
        @DisplayName("[성공] Search 쿠키가 있을 때 쿼리 파라미터 추가")
        void shouldAddQueryParamsWithSearchCookies() {
            // Given
            CrawlContext context = createContextWithSearchCookies();
            String searchEndpoint =
                    "https://api.example.com/search?nid=test-nid&uid=test-mustit-uid&adId=test-mustit-uid&beforeItemType=Normal";
            HttpResponse response = HttpResponse.of(200, "{\"items\": []}");
            CrawlResult expectedResult = CrawlResult.success("{\"items\": []}", 200);

            given(crawlContextMapper.buildSearchEndpoint(context)).willReturn(searchEndpoint);
            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).get(captor.capture());

            String requestUrl = captor.getValue().url();
            assertThat(requestUrl).contains("nid=test-nid");
            assertThat(requestUrl).contains("uid=test-mustit-uid");
            assertThat(requestUrl).contains("adId=test-mustit-uid");
            assertThat(requestUrl).contains("beforeItemType=Normal");
        }

        @Test
        @DisplayName("[성공] Search 쿠키가 없을 때 기본 엔드포인트 사용")
        void shouldUseDefaultEndpointWithoutSearchCookies() {
            // Given
            CrawlContext context = createContextWithoutSearchCookies();
            String searchEndpoint = "https://api.example.com/search";
            HttpResponse response = HttpResponse.of(200, "{\"items\": []}");
            CrawlResult expectedResult = CrawlResult.success("{\"items\": []}", 200);

            given(crawlContextMapper.buildSearchEndpoint(context)).willReturn(searchEndpoint);
            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).get(captor.capture());

            String requestUrl = captor.getValue().url();
            assertThat(requestUrl).doesNotContain("nid=");
            assertThat(requestUrl).doesNotContain("uid=");
        }

        @Test
        @DisplayName("[성공] HTTP 요청 후 성공 결과 반환")
        void shouldReturnSuccessResult() {
            // Given
            CrawlContext context = createContextWithSearchCookies();
            String searchEndpoint =
                    "https://api.example.com/search?nid=test-nid&uid=test-mustit-uid&adId=test-mustit-uid&beforeItemType=Normal";
            HttpResponse response = HttpResponse.of(200, "{\"items\": [], \"nextApiUrl\": null}");
            CrawlResult expectedResult =
                    CrawlResult.success("{\"items\": [], \"nextApiUrl\": null}", 200);

            given(crawlContextMapper.buildSearchEndpoint(context)).willReturn(searchEndpoint);
            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.responseBody()).contains("items");
        }

        @Test
        @DisplayName("[실패] HTTP 에러 시 실패 결과 반환")
        void shouldReturnFailureOnHttpError() {
            // Given
            CrawlContext context = createContextWithSearchCookies();
            String searchEndpoint =
                    "https://api.example.com/search?nid=test-nid&uid=test-mustit-uid&adId=test-mustit-uid&beforeItemType=Normal";
            HttpResponse response = HttpResponse.of(500, "Internal Server Error");
            CrawlResult expectedResult = CrawlResult.failure(500, "Server error: 500");

            given(crawlContextMapper.buildSearchEndpoint(context)).willReturn(searchEndpoint);
            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(500);
        }

        @Test
        @DisplayName("[성공] 기존 쿼리 파라미터가 있는 URL에 Search 파라미터 추가")
        void shouldAppendToExistingQueryParams() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            10L,
                            100L,
                            CrawlTaskType.SEARCH,
                            "https://api.example.com/search?page=1&size=20",
                            1L,
                            "Mozilla/5.0",
                            "session-token",
                            "test-nid",
                            "test-mustit-uid");
            String searchEndpoint =
                    "https://api.example.com/search?page=1&size=20&nid=test-nid&uid=test-mustit-uid&adId=test-mustit-uid&beforeItemType=Normal";
            HttpResponse response = HttpResponse.of(200, "{}");
            CrawlResult expectedResult = CrawlResult.success("{}", 200);

            given(crawlContextMapper.buildSearchEndpoint(context)).willReturn(searchEndpoint);
            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            crawler.crawl(context);

            // Then
            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).get(captor.capture());

            String requestUrl = captor.getValue().url();
            assertThat(requestUrl).contains("page=1&size=20");
            assertThat(requestUrl).contains("&nid=test-nid");
        }
    }

    // === Helper Methods ===

    private CrawlContext createContextWithSearchCookies() {
        return new CrawlContext(
                1L,
                10L,
                100L,
                CrawlTaskType.SEARCH,
                "https://api.example.com/search",
                1L,
                "Mozilla/5.0",
                "session-token-123",
                "test-nid",
                "test-mustit-uid");
    }

    private CrawlContext createContextWithoutSearchCookies() {
        return new CrawlContext(
                1L,
                10L,
                100L,
                CrawlTaskType.SEARCH,
                "https://api.example.com/search",
                1L,
                "Mozilla/5.0",
                "session-token-123",
                null,
                null);
    }
}
