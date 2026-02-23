package com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@DisplayName("CrawlResultMapper 단위 테스트")
class CrawlResultMapperTest {

    private final CrawlResultMapper mapper = new CrawlResultMapper();

    @Nested
    @DisplayName("toCrawlResult() 메서드는")
    class ToCrawlResult {

        @Test
        @DisplayName("2xx 응답이면 성공 결과를 반환한다")
        void shouldReturnSuccessForOkResponse() {
            // Given
            HttpResponse response = HttpResponse.of(200, "{\"items\":[]}");

            // When
            CrawlResult result = mapper.toCrawlResult(response);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.responseBody()).isEqualTo("{\"items\":[]}");
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("429 응답이면 Rate limited 에러를 반환한다")
        void shouldReturnRateLimitedFor429() {
            // Given
            HttpResponse response = HttpResponse.of(429, null);

            // When
            CrawlResult result = mapper.toCrawlResult(response);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(429);
            assertThat(result.errorMessage()).isEqualTo("Rate limited (429)");
        }

        @Test
        @DisplayName("5xx 응답이면 Server error를 반환한다")
        void shouldReturnServerErrorFor5xx() {
            // Given
            HttpResponse response = HttpResponse.of(503, null);

            // When
            CrawlResult result = mapper.toCrawlResult(response);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(503);
            assertThat(result.errorMessage()).isEqualTo("Server error: 503");
        }

        @Test
        @DisplayName("4xx 응답이면 Client error를 반환한다")
        void shouldReturnClientErrorFor4xx() {
            // Given
            HttpResponse response = HttpResponse.of(404, null);

            // When
            CrawlResult result = mapper.toCrawlResult(response);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(404);
            assertThat(result.errorMessage()).isEqualTo("Client error: 404");
        }
    }
}
