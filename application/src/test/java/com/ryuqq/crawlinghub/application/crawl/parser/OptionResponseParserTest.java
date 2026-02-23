package com.ryuqq.crawlinghub.application.crawl.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.OptionResponseParser;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * OptionResponseParser 단위 테스트
 *
 * <p>OPTION 크롤링 응답 파서 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("OptionResponseParser 테스트")
class OptionResponseParserTest {

    private OptionResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new OptionResponseParser(new ObjectMapper());
    }

    @Nested
    @DisplayName("parse() 테스트")
    class Parse {

        @Test
        @DisplayName("[성공] 정상 옵션 배열 파싱")
        void shouldParseValidOptionArray() {
            // Given
            String responseBody =
                    """
[
    {"optionNo": 1001, "itemNo": 123, "color": "Red", "size": "M", "stock": 10, "sizeGuide": ""},
    {"optionNo": 1002, "itemNo": 123, "color": "Blue", "size": "L", "stock": 5, "sizeGuide": ""}
]
""";

            // When
            List<ProductOption> result = parser.parse(responseBody, 123L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).optionNo()).isEqualTo(1001L);
            assertThat(result.get(0).color()).isEqualTo("Red");
            assertThat(result.get(0).size()).isEqualTo("M");
            assertThat(result.get(0).stock()).isEqualTo(10);
        }

        @Test
        @DisplayName("[성공] itemNo 없으면 context에서 사용")
        void shouldUseContextItemNoWhenMissing() {
            // Given
            String responseBody =
                    """
                    [
                        {"optionNo": 1001, "color": "Red", "size": "M", "stock": 10}
                    ]
                    """;

            // When
            List<ProductOption> result = parser.parse(responseBody, 999L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).itemNo()).isEqualTo(999L);
        }

        @Test
        @DisplayName("[실패] null 응답")
        void shouldReturnEmptyForNullResponse() {
            // When
            List<ProductOption> result = parser.parse(null, 123L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 빈 문자열 응답")
        void shouldReturnEmptyForBlankResponse() {
            // When
            List<ProductOption> result = parser.parse("  ", 123L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 배열이 아닌 응답")
        void shouldReturnEmptyForNonArrayResponse() {
            // Given
            String responseBody =
                    """
                    {"optionNo": 1001}
                    """;

            // When
            List<ProductOption> result = parser.parse(responseBody, 123L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] optionNo 없는 항목 스킵")
        void shouldSkipItemsWithoutOptionNo() {
            // Given
            String responseBody =
                    """
                    [
                        {"itemNo": 123, "color": "Red", "size": "M", "stock": 10},
                        {"optionNo": 1001, "itemNo": 123, "color": "Blue", "size": "L", "stock": 5}
                    ]
                    """;

            // When
            List<ProductOption> result = parser.parse(responseBody, 123L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).optionNo()).isEqualTo(1001L);
        }

        @Test
        @DisplayName("[성공] 빈 배열 응답")
        void shouldReturnEmptyForEmptyArray() {
            // Given
            String responseBody = "[]";

            // When
            List<ProductOption> result = parser.parse(responseBody, 123L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 잘못된 JSON 형식")
        void shouldReturnEmptyForInvalidJson() {
            // Given
            String responseBody = "invalid json";

            // When
            List<ProductOption> result = parser.parse(responseBody, 123L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] 숫자가 문자열로 온 경우 파싱")
        void shouldParseStringNumbers() {
            // Given
            String responseBody =
                    """
[
    {"optionNo": "1001", "itemNo": "123", "color": "Red", "size": "M", "stock": "10"}
]
""";

            // When
            List<ProductOption> result = parser.parse(responseBody, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).optionNo()).isEqualTo(1001L);
            assertThat(result.get(0).itemNo()).isEqualTo(123L);
        }
    }
}
