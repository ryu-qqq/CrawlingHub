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
 * MetaCrawler 단위 테스트
 *
 * <p>미니샵 메타 정보 크롤러 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetaCrawler 테스트")
class MetaCrawlerTest {

    @Mock private HttpClientPort httpClientPort;

    private MetaCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new MetaCrawler(httpClientPort);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] META 타입 반환")
        void shouldReturnMetaType() {
            // When
            CrawlTaskType result = crawler.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.META);
        }
    }

    @Nested
    @DisplayName("supports() 테스트")
    class Supports {

        @Test
        @DisplayName("[성공] META 타입 지원")
        void shouldSupportMetaType() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.META)).isTrue();
        }

        @Test
        @DisplayName("[성공] 다른 타입 미지원")
        void shouldNotSupportOtherTypes() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.DETAIL)).isFalse();
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
            HttpResponse response = HttpResponse.of(200, "{\"shop\": \"meta\"}");
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getHttpStatusCode()).isEqualTo(200);
            assertThat(result.getResponseBody()).isEqualTo("{\"shop\": \"meta\"}");
            verify(httpClientPort).get(any(HttpRequest.class));
        }

        @Test
        @DisplayName("[실패] HTTP 에러 시 실패 결과 반환")
        void shouldReturnFailureOnHttpError() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(503, "Service Unavailable");
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getHttpStatusCode()).isEqualTo(503);
        }
    }

    // === Helper Methods ===

    private CrawlContext createContext() {
        return new CrawlContext(
                1L,
                10L,
                100L,
                CrawlTaskType.META,
                "https://api.example.com/shops/123/meta",
                1L,
                "Mozilla/5.0",
                "session-token-123",
                null,
                null);
    }
}
