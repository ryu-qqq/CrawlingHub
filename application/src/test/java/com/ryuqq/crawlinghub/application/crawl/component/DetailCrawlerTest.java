package com.ryuqq.crawlinghub.application.crawl.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlContext;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.crawl.dto.HttpResponse;
import com.ryuqq.crawlinghub.application.crawl.port.out.client.HttpClientPort;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
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

    @Mock private HttpClientPort httpClientPort;

    private DetailCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new DetailCrawler(httpClientPort);
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
            assertThat(crawler.supports(CrawlTaskType.META)).isFalse();
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
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getHttpStatusCode()).isEqualTo(200);
            assertThat(result.getResponseBody()).isEqualTo("{\"product\": \"detail\"}");
            verify(httpClientPort).get(any(HttpRequest.class));
        }

        @Test
        @DisplayName("[실패] HTTP 4xx 에러 시 실패 결과 반환")
        void shouldReturnFailureOnClientError() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(404, "Not Found");
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getHttpStatusCode()).isEqualTo(404);
            assertThat(result.getErrorMessage()).contains("Client error");
        }

        @Test
        @DisplayName("[실패] HTTP 5xx 에러 시 실패 결과 반환")
        void shouldReturnFailureOnServerError() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(500, "Internal Server Error");
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getHttpStatusCode()).isEqualTo(500);
            assertThat(result.getErrorMessage()).contains("Server error");
        }

        @Test
        @DisplayName("[실패] Rate Limit 에러 시 실패 결과 반환")
        void shouldReturnFailureOnRateLimit() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(429, "Too Many Requests");
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getHttpStatusCode()).isEqualTo(429);
            assertThat(result.getErrorMessage()).contains("Rate limited");
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
