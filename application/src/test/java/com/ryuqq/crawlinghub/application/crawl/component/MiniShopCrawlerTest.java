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
 * MiniShopCrawler 단위 테스트
 *
 * <p>미니샵 상품 목록 크롤러 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MiniShopCrawler 테스트")
class MiniShopCrawlerTest {

    @Mock private HttpClientPort httpClientPort;

    private MiniShopCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new MiniShopCrawler(httpClientPort);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] MINI_SHOP 타입 반환")
        void shouldReturnMiniShopType() {
            // When
            CrawlTaskType result = crawler.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.MINI_SHOP);
        }
    }

    @Nested
    @DisplayName("supports() 테스트")
    class Supports {

        @Test
        @DisplayName("[성공] MINI_SHOP 타입 지원")
        void shouldSupportMiniShopType() {
            // When & Then
            assertThat(crawler.supports(CrawlTaskType.MINI_SHOP)).isTrue();
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
            HttpResponse response = HttpResponse.of(200, "{\"products\": []}");
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getHttpStatusCode()).isEqualTo(200);
            assertThat(result.getResponseBody()).isEqualTo("{\"products\": []}");
            verify(httpClientPort).get(any(HttpRequest.class));
        }

        @Test
        @DisplayName("[실패] HTTP 에러 시 실패 결과 반환")
        void shouldReturnFailureOnHttpError() {
            // Given
            CrawlContext context = createContext();
            HttpResponse response = HttpResponse.of(502, "Bad Gateway");
            given(httpClientPort.get(any(HttpRequest.class))).willReturn(response);

            // When
            CrawlResult result = crawler.crawl(context);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getHttpStatusCode()).isEqualTo(502);
            assertThat(result.getErrorMessage()).contains("Server error");
        }
    }

    // === Helper Methods ===

    private CrawlContext createContext() {
        return new CrawlContext(
                1L,
                10L,
                100L,
                CrawlTaskType.MINI_SHOP,
                "https://api.example.com/shops/123/products",
                1L,
                "Mozilla/5.0",
                "session-token-123",
                null,
                null);
    }
}
