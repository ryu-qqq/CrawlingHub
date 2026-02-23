package com.ryuqq.crawlinghub.adapter.out.http.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * HttpResponseMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("HttpResponseMapper 테스트")
class HttpResponseMapperTest {

    private final HttpResponseMapper mapper = new HttpResponseMapper();

    @Nested
    @DisplayName("toHttpResponse")
    class ToHttpResponse {

        @Test
        @DisplayName("성공 응답을 HttpResponse로 변환")
        void shouldConvertSuccessResponse() {
            // given
            ClientResponse clientResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body("response body")
                            .build();

            // when
            HttpResponse response = mapper.toHttpResponse(clientResponse).block();

            // then
            assertThat(response).isNotNull();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("response body");
            assertThat(response.headers()).containsKey("Content-Type");
        }

        @Test
        @DisplayName("빈 바디 응답을 빈 문자열로 변환")
        void shouldConvertEmptyBodyToEmptyString() {
            // given
            ClientResponse clientResponse = ClientResponse.create(HttpStatus.NO_CONTENT).build();

            // when
            HttpResponse response = mapper.toHttpResponse(clientResponse).block();

            // then
            assertThat(response).isNotNull();
            assertThat(response.statusCode()).isEqualTo(204);
            assertThat(response.body()).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromException")
    class FromException {

        @Test
        @DisplayName("WebClientResponseException을 HttpResponse로 변환")
        void shouldConvertWebClientResponseException() {
            // given
            WebClientResponseException exception =
                    WebClientResponseException.create(
                            404, "Not Found", HttpHeaders.EMPTY, "error body".getBytes(), null);

            // when
            HttpResponse response = mapper.fromException(exception);

            // then
            assertThat(response.statusCode()).isEqualTo(404);
            assertThat(response.body()).isEqualTo("error body");
        }
    }

    @Nested
    @DisplayName("fromConnectionFailure")
    class FromConnectionFailure {

        @Test
        @DisplayName("연결 실패를 HttpResponse로 변환")
        void shouldConvertConnectionFailure() {
            // given
            Exception exception = new RuntimeException("Connection refused");

            // when
            HttpResponse response = mapper.fromConnectionFailure(exception);

            // then
            assertThat(response.statusCode()).isZero();
            assertThat(response.body()).isEqualTo("Connection failed: Connection refused");
        }
    }
}
