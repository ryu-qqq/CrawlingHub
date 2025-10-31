package com.ryuqq.crawlinghub.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProductData Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("ProductData Value Object 단위 테스트")
class ProductDataTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("유효한 JSON 데이터로 ProductData 생성 성공")
        void shouldCreateWithValidJsonData() {
            // Given
            String jsonData = "{\"productId\": 12345, \"name\": \"테스트 상품\", \"price\": 10000}";

            // When
            ProductData productData = ProductData.of(jsonData);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(jsonData);
        }

        @Test
        @DisplayName("복잡한 JSON 객체로 ProductData 생성 성공")
        void shouldCreateWithComplexJson() {
            // Given
            String complexJson = """
                {
                    "productId": 12345,
                    "name": "테스트 상품",
                    "price": 10000,
                    "options": [
                        {"name": "색상", "value": "빨강"},
                        {"name": "크기", "value": "L"}
                    ],
                    "metadata": {
                        "brand": "테스트브랜드",
                        "origin": "한국"
                    }
                }
                """;

            // When
            ProductData productData = ProductData.of(complexJson);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(complexJson);
        }

        @Test
        @DisplayName("JSON 배열 데이터로 ProductData 생성 성공")
        void shouldCreateWithJsonArray() {
            // Given
            String jsonArray = "[{\"id\": 1, \"name\": \"상품1\"}, {\"id\": 2, \"name\": \"상품2\"}]";

            // When
            ProductData productData = ProductData.of(jsonArray);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(jsonArray);
        }

        @Test
        @DisplayName("단순 문자열도 ProductData로 생성 가능")
        void shouldCreateWithSimpleString() {
            // Given
            String simpleString = "상품 설명 텍스트";

            // When
            ProductData productData = ProductData.of(simpleString);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(simpleString);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("JSON 데이터가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenJsonDataIsNullOrBlank(String invalidData) {
            // When & Then
            assertThatThrownBy(() -> ProductData.of(invalidData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON 데이터는 필수입니다");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 JSON 데이터를 가진 두 ProductData는 isSameAs() 가 true 반환")
        void shouldReturnTrueForSameJsonData() {
            // Given
            String jsonData = "{\"productId\": 12345, \"name\": \"테스트 상품\"}";
            ProductData data1 = ProductData.of(jsonData);
            ProductData data2 = ProductData.of(jsonData);

            // When
            boolean result = data1.isSameAs(data2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 JSON 데이터를 가진 두 ProductData는 isSameAs() 가 false 반환")
        void shouldReturnFalseForDifferentJsonData() {
            // Given
            ProductData data1 = ProductData.of("{\"productId\": 12345}");
            ProductData data2 = ProductData.of("{\"productId\": 67890}");

            // When
            boolean result = data1.isSameAs(data2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null과 비교하면 isSameAs() 가 false 반환")
        void shouldReturnFalseWhenComparedWithNull() {
            // Given
            ProductData productData = ProductData.of("{\"productId\": 12345}");

            // When
            boolean result = productData.isSameAs(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("같은 JSON 데이터를 가진 두 ProductData는 equals() 가 true 반환")
        void shouldReturnTrueForEquals() {
            // Given
            String jsonData = "{\"productId\": 12345, \"name\": \"테스트 상품\"}";
            ProductData data1 = ProductData.of(jsonData);
            ProductData data2 = ProductData.of(jsonData);

            // When & Then
            assertThat(data1).isEqualTo(data2);
        }

        @Test
        @DisplayName("같은 JSON 데이터를 가진 두 ProductData는 같은 hashCode 반환")
        void shouldReturnSameHashCode() {
            // Given
            String jsonData = "{\"productId\": 12345, \"name\": \"테스트 상품\"}";
            ProductData data1 = ProductData.of(jsonData);
            ProductData data2 = ProductData.of(jsonData);

            // When & Then
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("다른 JSON 데이터를 가진 두 ProductData는 다른 hashCode 반환")
        void shouldReturnDifferentHashCode() {
            // Given
            ProductData data1 = ProductData.of("{\"productId\": 12345}");
            ProductData data2 = ProductData.of("{\"productId\": 67890}");

            // When & Then
            assertThat(data1.hashCode()).isNotEqualTo(data2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 JSON 데이터를 포함한 문자열 반환")
        void shouldReturnStringWithJsonData() {
            // Given
            String jsonData = "{\"productId\": 12345, \"name\": \"테스트 상품\"}";
            ProductData productData = ProductData.of(jsonData);

            // When
            String result = productData.toString();

            // Then
            assertThat(result).contains("productId");
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("매우 큰 JSON 데이터도 정상 생성")
        void shouldCreateWithVeryLargeJson() {
            // Given
            StringBuilder largeJson = new StringBuilder("{\"items\": [");
            for (int i = 0; i < 1000; i++) {
                if (i > 0) largeJson.append(",");
                largeJson.append("{\"id\": ").append(i).append(", \"name\": \"상품").append(i).append("\"}");
            }
            largeJson.append("]}");

            // When
            ProductData productData = ProductData.of(largeJson.toString());

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).hasSize(largeJson.length());
        }

        @Test
        @DisplayName("특수 문자가 포함된 JSON 데이터도 정상 생성")
        void shouldCreateWithSpecialCharacters() {
            // Given
            String jsonWithSpecialChars = "{\"name\": \"상품\\n이름\", \"description\": \"설명\\t내용\"}";

            // When
            ProductData productData = ProductData.of(jsonWithSpecialChars);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(jsonWithSpecialChars);
        }

        @Test
        @DisplayName("유니코드 문자가 포함된 JSON 데이터도 정상 생성")
        void shouldCreateWithUnicodeCharacters() {
            // Given
            String jsonWithUnicode = "{\"emoji\": \"😀\", \"korean\": \"한글\", \"japanese\": \"日本語\"}";

            // When
            ProductData productData = ProductData.of(jsonWithUnicode);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(jsonWithUnicode);
        }

        @Test
        @DisplayName("중첩된 JSON 객체도 정상 생성")
        void shouldCreateWithNestedJson() {
            // Given
            String nestedJson = """
                {
                    "level1": {
                        "level2": {
                            "level3": {
                                "value": "깊은 중첩"
                            }
                        }
                    }
                }
                """;

            // When
            ProductData productData = ProductData.of(nestedJson);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(nestedJson);
        }

        @Test
        @DisplayName("빈 JSON 객체도 정상 생성")
        void shouldCreateWithEmptyJsonObject() {
            // Given
            String emptyJson = "{}";

            // When
            ProductData productData = ProductData.of(emptyJson);

            // Then
            assertThat(productData).isNotNull();
            assertThat(productData.getValue()).isEqualTo(emptyJson);
        }
    }
}
