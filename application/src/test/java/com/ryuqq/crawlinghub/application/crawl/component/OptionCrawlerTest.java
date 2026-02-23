package com.ryuqq.crawlinghub.application.crawl.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.OptionCrawler;
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
 * OptionCrawler 단위 테스트
 *
 * <p>상품 옵션 정보 크롤러 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OptionCrawler 테스트")
class OptionCrawlerTest {

    @Mock private HttpClient httpClient;
    @Mock private CrawlContextMapper crawlContextMapper;
    @Mock private CrawlResultMapper crawlResultMapper;

    private OptionCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new OptionCrawler(httpClient, crawlContextMapper, crawlResultMapper);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] OPTION 타입 반환")
        void shouldReturnOptionType() {
            // When
            CrawlTaskType result = crawler.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.OPTION);
        }
    }

    @Nested
    @DisplayName("supports() 테스트")
    class Supports {

        @Test
        @DisplayName("[성공] OPTION 타입 지원")
        void shouldSupportOptionType() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.OPTION)).isTrue();
        }

        @Test
        @DisplayName("[성공] 다른 타입 미지원")
        void shouldNotSupportOtherTypes() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.DETAIL)).isFalse();
            assertThat(crawler.supports(CrawlTaskType.SEARCH)).isFalse();
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
            HttpResponse response = HttpResponse.of(200, "{\"options\": []}");
            CrawlResult expectedResult = CrawlResult.success("{\"options\": []}", 200);

            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.responseBody()).isEqualTo("{\"options\": []}");
            verify(httpClient).get(any(HttpRequest.class));
        }

        @Test
        @DisplayName("[실패] HTTP 에러 시 실패 결과 반환")
        void shouldReturnFailureOnHttpError() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(400, "Bad Request");
            CrawlResult expectedResult = CrawlResult.failure(400, "Client error: 400");

            given(crawlContextMapper.buildHeaders(context))
                    .willReturn(Map.of("User-Agent", "Mozilla/5.0"));
            given(httpClient.get(any(HttpRequest.class))).willReturn(response);
            given(crawlResultMapper.toCrawlResult(response)).willReturn(expectedResult);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(400);
            assertThat(result.errorMessage()).contains("Client error");
        }
    }

    // === Helper Methods ===

    private CrawlContext createContext() {
        return new CrawlContext(
                1L,
                10L,
                100L,
                CrawlTaskType.OPTION,
                "https://api.example.com/products/123/options",
                1L,
                "Mozilla/5.0",
                "session-token-123",
                null,
                null);
    }
}
