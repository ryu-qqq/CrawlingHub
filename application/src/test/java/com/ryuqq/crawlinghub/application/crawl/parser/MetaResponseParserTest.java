package com.ryuqq.crawlinghub.application.crawl.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCount;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * MetaResponseParser 단위 테스트
 *
 * <p>META 크롤링 응답 파서 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("MetaResponseParser 테스트")
class MetaResponseParserTest {

    private MetaResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new MetaResponseParser(new ObjectMapper());
    }

    @Nested
    @DisplayName("parseResponse() 테스트")
    class ParseResponse {

        @Test
        @DisplayName("[성공] 정상 응답 파싱")
        void shouldParseValidResponse() {
            // Given
            String responseBody =
                    """
                    {"count": 150}
                    """;

            // When
            Optional<ProductCount> result = parser.parseResponse(responseBody);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().totalCount()).isEqualTo(150);
        }

        @Test
        @DisplayName("[성공] count가 0인 경우")
        void shouldParseZeroCount() {
            // Given
            String responseBody =
                    """
                    {"count": 0}
                    """;

            // When
            Optional<ProductCount> result = parser.parseResponse(responseBody);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().totalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("[실패] null 응답")
        void shouldReturnEmptyForNullResponse() {
            // When
            Optional<ProductCount> result = parser.parseResponse(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 빈 문자열 응답")
        void shouldReturnEmptyForBlankResponse() {
            // When
            Optional<ProductCount> result = parser.parseResponse("  ");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] count 필드 없음")
        void shouldReturnEmptyWhenNoCountField() {
            // Given
            String responseBody =
                    """
                    {"total": 150}
                    """;

            // When
            Optional<ProductCount> result = parser.parseResponse(responseBody);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] count가 숫자가 아닌 경우")
        void shouldReturnEmptyWhenCountNotNumber() {
            // Given
            String responseBody =
                    """
                    {"count": "not a number"}
                    """;

            // When
            Optional<ProductCount> result = parser.parseResponse(responseBody);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 잘못된 JSON 형식")
        void shouldReturnEmptyForInvalidJson() {
            // Given
            String responseBody = "invalid json";

            // When
            Optional<ProductCount> result = parser.parseResponse(responseBody);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
