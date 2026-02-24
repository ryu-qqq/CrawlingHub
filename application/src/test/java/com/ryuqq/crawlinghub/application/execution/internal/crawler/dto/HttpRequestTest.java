package com.ryuqq.crawlinghub.application.execution.internal.crawler.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * HttpRequest 단위 테스트
 *
 * <p>HTTP 요청 DTO 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("HttpRequest 테스트")
class HttpRequestTest {

    @Nested
    @DisplayName("get(url) 테스트")
    class GetWithUrl {

        @Test
        @DisplayName("[성공] GET 요청 생성 - 헤더 없음")
        void shouldCreateGetRequestWithoutHeaders() {
            HttpRequest request = HttpRequest.get("https://example.com");

            assertThat(request.url()).isEqualTo("https://example.com");
            assertThat(request.headers()).isEmpty();
            assertThat(request.body()).isNull();
        }
    }

    @Nested
    @DisplayName("get(url, headers) 테스트")
    class GetWithHeaders {

        @Test
        @DisplayName("[성공] GET 요청 생성 - 헤더 포함")
        void shouldCreateGetRequestWithHeaders() {
            Map<String, String> headers = Map.of("Authorization", "Bearer token");
            HttpRequest request = HttpRequest.get("https://example.com", headers);

            assertThat(request.url()).isEqualTo("https://example.com");
            assertThat(request.headers()).containsEntry("Authorization", "Bearer token");
            assertThat(request.body()).isNull();
        }

        @Test
        @DisplayName("[성공] null 헤더이면 빈 맵으로 처리")
        void shouldHandleNullHeadersInGetRequest() {
            HttpRequest request = new HttpRequest("https://example.com", null, null);

            assertThat(request.headers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("post(url, body) 테스트")
    class PostWithBody {

        @Test
        @DisplayName("[성공] POST 요청 생성 - 헤더 없음")
        void shouldCreatePostRequestWithBody() {
            HttpRequest request =
                    HttpRequest.post("https://example.com/api", "{\"key\":\"value\"}");

            assertThat(request.url()).isEqualTo("https://example.com/api");
            assertThat(request.headers()).isEmpty();
            assertThat(request.body()).isEqualTo("{\"key\":\"value\"}");
        }
    }

    @Nested
    @DisplayName("post(url, headers, body) 테스트")
    class PostWithHeadersAndBody {

        @Test
        @DisplayName("[성공] POST 요청 생성 - 헤더 포함")
        void shouldCreatePostRequestWithHeadersAndBody() {
            Map<String, String> headers = Map.of("Content-Type", "application/json");
            HttpRequest request =
                    HttpRequest.post("https://example.com/api", headers, "{\"key\":\"value\"}");

            assertThat(request.url()).isEqualTo("https://example.com/api");
            assertThat(request.headers()).containsEntry("Content-Type", "application/json");
            assertThat(request.body()).isEqualTo("{\"key\":\"value\"}");
        }
    }
}
