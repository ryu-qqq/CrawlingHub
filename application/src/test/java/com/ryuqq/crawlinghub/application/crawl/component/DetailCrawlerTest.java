package com.ryuqq.crawlinghub.application.crawl.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.DetailCrawler;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DetailCrawler 단위 테스트
 *
 * <p>상품 상세 정보 크롤러 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DetailCrawler 테스트")
class DetailCrawlerTest {

    @Mock private HttpClient httpClient;
    @Mock private CrawlContextMapper crawlContextMapper;
    @Mock private CrawlResultMapper crawlResultMapper;

    private DetailCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new DetailCrawler(httpClient, crawlContextMapper, crawlResultMapper);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] DETAIL 타입 반환")
        void shouldReturnDetailType() {
            // When
            CrawlTaskType result = crawler.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.DETAIL);
        }
    }

    @Nested
    @DisplayName("supports() 테스트")
    class Supports {

        @Test
        @DisplayName("[성공] DETAIL 타입 지원")
        void shouldSupportDetailType() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.DETAIL)).isTrue();
        }

        @Test
        @DisplayName("[성공] 다른 타입 미지원")
        void shouldNotSupportOtherTypes() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.SEARCH)).isFalse();
            assertThat(crawler.supports(CrawlTaskType.OPTION)).isFalse();
        }
    }

    @Nested
    @DisplayName("crawl() 테스트")
    class Crawl {

        @Test
        @DisplayName("[성공] HTTP 요청 후 성공 결과 반환")
        void shouldReturnSuccessResult() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(200, "{\"product\": \"detail\"}");
            CrawlResult expectedResult = CrawlResult.success("{\"product\": \"detail\"}", 200);

            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.responseBody()).isEqualTo("{\"product\": \"detail\"}");
            verify(httpClient).get(any(HttpRequest.class));
        }

        @Test
        @DisplayName("[실패] HTTP 4xx 에러 시 실패 결과 반환")
        void shouldReturnFailureOnClientError() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(404, "Not Found");
            CrawlResult expectedResult = CrawlResult.failure(404, "Client error: 404");

            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(404);
            assertThat(result.errorMessage()).contains("Client error");
        }

        @Test
        @DisplayName("[실패] HTTP 5xx 에러 시 실패 결과 반환")
        void shouldReturnFailureOnServerError() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(500, "Internal Server Error");
            CrawlResult expectedResult = CrawlResult.failure(500, "Server error: 500");

            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(500);
            assertThat(result.errorMessage()).contains("Server error");
        }

        @Test
        @DisplayName("[실패] Rate Limit 에러 시 실패 결과 반환")
        void shouldReturnFailureOnRateLimit() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(429, "Too Many Requests");
            CrawlResult expectedResult = CrawlResult.failure(429, "Rate limited (429)");

            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(429);
            assertThat(result.errorMessage()).contains("Rate limited");
        }
    }

    // === Helper Methods ===

    private CrawlContext createContext() {
        return new CrawlContext(
                1L, // crawlTaskId
                10L, // schedulerId
                100L, // sellerId
                CrawlTaskType.DETAIL,
                "https://api.example.com/products/123",
                1L, // userAgentId
                "Mozilla/5.0",
                "session-token-123",
                null, // nid
                null // mustitUid
                );
    }
}
