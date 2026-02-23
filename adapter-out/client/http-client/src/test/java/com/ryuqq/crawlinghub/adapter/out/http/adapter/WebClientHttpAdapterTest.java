package com.ryuqq.crawlinghub.adapter.out.http.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ryuqq.crawlinghub.adapter.out.http.mapper.HttpResponseMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * WebClientHttpAdapter 단위 테스트
 *
 * <p>WebClient를 모킹하여 HTTP 요청/응답 처리를 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebClientHttpAdapter 테스트")
class WebClientHttpAdapterTest {

    @Mock private WebClient webClient;

    @Mock private HttpResponseMapper mapper;

    private WebClientHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WebClientHttpAdapter(webClient, mapper);
    }

    @Nested
    @DisplayName("get 메서드 테스트")
    class GetMethodTest {

        @Test
        @DisplayName("GET 요청 성공 시 HttpResponse를 반환한다")
        void get_withSuccessResponse_returnsHttpResponse() {
            // given
            HttpRequest request = HttpRequest.get("https://example.com/api");
            HttpResponse expectedResponse = HttpResponse.of(200, "response body");

            // WebClient 모킹 체인 설정
            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(any(String.class))).thenReturn(headersSpec);
            when(headersSpec.headers(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenReturn(Mono.just(expectedResponse));

            // when
            HttpResponse result = adapter.get(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.statusCode()).isEqualTo(200);
            assertThat(result.body()).isEqualTo("response body");
        }

        @Test
        @DisplayName("GET 요청 시 WebClientResponseException 발생 시 mapper.fromException을 호출한다")
        void get_whenWebClientResponseException_callsMapperFromException() {
            // given
            HttpRequest request = HttpRequest.get("https://example.com/api");
            HttpResponse errorResponse = HttpResponse.of(404, "Not Found");

            WebClientResponseException exception =
                    WebClientResponseException.create(
                            404,
                            "Not Found",
                            org.springframework.http.HttpHeaders.EMPTY,
                            "Not Found".getBytes(),
                            null);

            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(any(String.class))).thenReturn(headersSpec);
            when(headersSpec.headers(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(exception);
            when(mapper.fromException(exception)).thenReturn(errorResponse);

            // when
            HttpResponse result = adapter.get(request);

            // then
            assertThat(result.statusCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("GET 요청 시 일반 예외 발생 시 mapper.fromConnectionFailure를 호출한다")
        void get_whenGenericException_callsMapperFromConnectionFailure() {
            // given
            HttpRequest request = HttpRequest.get("https://example.com/api");
            HttpResponse connectionFailureResponse =
                    HttpResponse.of(0, "Connection failed: Connection refused");

            RuntimeException connectionError = new RuntimeException("Connection refused");

            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(any(String.class))).thenReturn(headersSpec);
            when(headersSpec.headers(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(connectionError);
            when(mapper.fromConnectionFailure(connectionError))
                    .thenReturn(connectionFailureResponse);

            // when
            HttpResponse result = adapter.get(request);

            // then
            assertThat(result.statusCode()).isZero();
            assertThat(result.body()).isEqualTo("Connection failed: Connection refused");
        }

        @Test
        @DisplayName("GET 요청 시 헤더가 포함된 요청을 처리한다")
        void get_withHeaders_processesRequestWithHeaders() {
            // given
            Map<String, String> headers =
                    Map.of(
                            "User-Agent", "TestBot/1.0",
                            "Cookie", "session=abc123");
            HttpRequest request = HttpRequest.get("https://example.com/api", headers);
            HttpResponse expectedResponse = HttpResponse.of(200, "response body");

            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(any(String.class))).thenReturn(headersSpec);
            when(headersSpec.headers(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenReturn(Mono.just(expectedResponse));

            // when
            HttpResponse result = adapter.get(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.statusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("post 메서드 테스트")
    class PostMethodTest {

        @Test
        @DisplayName("POST 요청 성공 시 HttpResponse를 반환한다")
        void post_withSuccessResponse_returnsHttpResponse() {
            // given
            HttpRequest request =
                    HttpRequest.post("https://example.com/api", "{\"key\":\"value\"}");
            HttpResponse expectedResponse = HttpResponse.of(201, "created");

            WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.post()).thenReturn(bodyUriSpec);
            when(bodyUriSpec.uri(any(String.class))).thenReturn(bodySpec);
            when(bodySpec.headers(any())).thenReturn(bodySpec);
            when(bodySpec.contentType(any())).thenReturn(bodySpec);
            when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenReturn(Mono.just(expectedResponse));

            // when
            HttpResponse result = adapter.post(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.statusCode()).isEqualTo(201);
        }

        @Test
        @DisplayName("body가 null인 POST 요청 시 빈 문자열로 처리한다")
        void post_withNullBody_usesEmptyString() {
            // given
            HttpRequest request = new HttpRequest("https://example.com/api", null, null);
            HttpResponse expectedResponse = HttpResponse.of(200, "ok");

            WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.post()).thenReturn(bodyUriSpec);
            when(bodyUriSpec.uri(any(String.class))).thenReturn(bodySpec);
            when(bodySpec.headers(any())).thenReturn(bodySpec);
            when(bodySpec.contentType(any())).thenReturn(bodySpec);
            when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenReturn(Mono.just(expectedResponse));

            // when
            HttpResponse result = adapter.post(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("POST 요청 시 WebClientResponseException 발생 시 mapper.fromException을 호출한다")
        void post_whenWebClientResponseException_callsMapperFromException() {
            // given
            HttpRequest request = HttpRequest.post("https://example.com/api", "body");
            HttpResponse errorResponse = HttpResponse.of(500, "Internal Server Error");

            WebClientResponseException exception =
                    WebClientResponseException.create(
                            500,
                            "Internal Server Error",
                            org.springframework.http.HttpHeaders.EMPTY,
                            "Internal Server Error".getBytes(),
                            null);

            WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.post()).thenReturn(bodyUriSpec);
            when(bodyUriSpec.uri(any(String.class))).thenReturn(bodySpec);
            when(bodySpec.headers(any())).thenReturn(bodySpec);
            when(bodySpec.contentType(any())).thenReturn(bodySpec);
            when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(exception);
            when(mapper.fromException(exception)).thenReturn(errorResponse);

            // when
            HttpResponse result = adapter.post(request);

            // then
            assertThat(result.statusCode()).isEqualTo(500);
        }

        @Test
        @DisplayName("POST 요청 시 일반 예외 발생 시 mapper.fromConnectionFailure를 호출한다")
        void post_whenGenericException_callsMapperFromConnectionFailure() {
            // given
            HttpRequest request = HttpRequest.post("https://example.com/api", "body");
            HttpResponse connectionFailureResponse =
                    HttpResponse.of(0, "Connection failed: timeout");

            RuntimeException timeoutError = new RuntimeException("timeout");

            WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.post()).thenReturn(bodyUriSpec);
            when(bodyUriSpec.uri(any(String.class))).thenReturn(bodySpec);
            when(bodySpec.headers(any())).thenReturn(bodySpec);
            when(bodySpec.contentType(any())).thenReturn(bodySpec);
            when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(timeoutError);
            when(mapper.fromConnectionFailure(timeoutError)).thenReturn(connectionFailureResponse);

            // when
            HttpResponse result = adapter.post(request);

            // then
            assertThat(result.statusCode()).isZero();
        }
    }
}
