package com.ryuqq.crawlinghub.domain.change;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ChangeData Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("ChangeData Value Object 단위 테스트")
class ChangeDataTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("유효한 변경 상세 정보로 ChangeData 생성 성공")
        void shouldCreateWithValidDetails() {
            // Given
            String details = "가격 변경: 10,000원 -> 12,000원";

            // When
            ChangeData changeData = ChangeData.of(details);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(details);
        }

        @Test
        @DisplayName("JSON 형식의 변경 정보로 ChangeData 생성 성공")
        void shouldCreateWithJsonFormatDetails() {
            // Given
            String jsonDetails = "{\"field\": \"price\", \"oldValue\": 10000, \"newValue\": 12000}";

            // When
            ChangeData changeData = ChangeData.of(jsonDetails);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(jsonDetails);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "재고 수량 변경: 100 -> 50",
            "상품명 변경: 기존상품 -> 신규상품",
            "카테고리 변경: 전자제품 -> 가전제품",
            "배송비 변경: 무료 -> 3,000원",
            "판매 상태 변경: 판매중 -> 품절"
        })
        @DisplayName("다양한 형식의 변경 정보로 ChangeData 생성 성공")
        void shouldCreateWithVariousDetailFormats(String details) {
            // When
            ChangeData changeData = ChangeData.of(details);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(details);
        }

        @Test
        @DisplayName("긴 변경 상세 정보도 정상 생성")
        void shouldCreateWithLongDetails() {
            // Given
            String longDetails = "상품 설명 변경: " + "매우 긴 상품 설명 텍스트입니다. ".repeat(100);

            // When
            ChangeData changeData = ChangeData.of(longDetails);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(longDetails);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("변경 상세 정보가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenDetailsIsNullOrBlank(String invalidDetails) {
            // When & Then
            assertThatThrownBy(() -> ChangeData.of(invalidDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경 상세 정보는 필수입니다");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 변경 정보를 가진 두 ChangeData는 isSameAs() 가 true 반환")
        void shouldReturnTrueForSameDetails() {
            // Given
            String details = "가격 변경: 10,000원 -> 12,000원";
            ChangeData data1 = ChangeData.of(details);
            ChangeData data2 = ChangeData.of(details);

            // When
            boolean result = data1.isSameAs(data2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 변경 정보를 가진 두 ChangeData는 isSameAs() 가 false 반환")
        void shouldReturnFalseForDifferentDetails() {
            // Given
            ChangeData data1 = ChangeData.of("가격 변경: 10,000원 -> 12,000원");
            ChangeData data2 = ChangeData.of("재고 변경: 100 -> 50");

            // When
            boolean result = data1.isSameAs(data2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null과 비교하면 isSameAs() 가 false 반환")
        void shouldReturnFalseWhenComparedWithNull() {
            // Given
            ChangeData changeData = ChangeData.of("가격 변경: 10,000원 -> 12,000원");

            // When
            boolean result = changeData.isSameAs(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("같은 변경 정보를 가진 두 ChangeData는 equals() 가 true 반환")
        void shouldReturnTrueForEquals() {
            // Given
            String details = "가격 변경: 10,000원 -> 12,000원";
            ChangeData data1 = ChangeData.of(details);
            ChangeData data2 = ChangeData.of(details);

            // When & Then
            assertThat(data1).isEqualTo(data2);
        }

        @Test
        @DisplayName("같은 변경 정보를 가진 두 ChangeData는 같은 hashCode 반환")
        void shouldReturnSameHashCode() {
            // Given
            String details = "가격 변경: 10,000원 -> 12,000원";
            ChangeData data1 = ChangeData.of(details);
            ChangeData data2 = ChangeData.of(details);

            // When & Then
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("다른 변경 정보를 가진 두 ChangeData는 다른 hashCode 반환")
        void shouldReturnDifferentHashCode() {
            // Given
            ChangeData data1 = ChangeData.of("가격 변경: 10,000원 -> 12,000원");
            ChangeData data2 = ChangeData.of("재고 변경: 100 -> 50");

            // When & Then
            assertThat(data1.hashCode()).isNotEqualTo(data2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 변경 상세 정보를 포함한 문자열 반환")
        void shouldReturnStringWithDetails() {
            // Given
            String details = "가격 변경: 10,000원 -> 12,000원";
            ChangeData changeData = ChangeData.of(details);

            // When
            String result = changeData.toString();

            // Then
            assertThat(result).contains(details);
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("특수 문자가 포함된 변경 정보도 정상 생성")
        void shouldCreateWithSpecialCharacters() {
            // Given
            String detailsWithSpecialChars = "설명 변경: 기존\\n내용 -> 신규\\t내용";

            // When
            ChangeData changeData = ChangeData.of(detailsWithSpecialChars);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(detailsWithSpecialChars);
        }

        @Test
        @DisplayName("유니코드 문자가 포함된 변경 정보도 정상 생성")
        void shouldCreateWithUnicodeCharacters() {
            // Given
            String detailsWithUnicode = "상품명 변경: 테스트😀 -> 새로운😎";

            // When
            ChangeData changeData = ChangeData.of(detailsWithUnicode);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(detailsWithUnicode);
        }

        @Test
        @DisplayName("복잡한 JSON 구조의 변경 정보도 정상 생성")
        void shouldCreateWithComplexJsonStructure() {
            // Given
            String complexJson = """
                {
                    "changes": [
                        {"field": "price", "old": 10000, "new": 12000},
                        {"field": "stock", "old": 100, "new": 50}
                    ],
                    "timestamp": "2025-01-30T10:00:00Z",
                    "user": "admin"
                }
                """;

            // When
            ChangeData changeData = ChangeData.of(complexJson);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(complexJson);
        }

        @Test
        @DisplayName("HTML 태그가 포함된 변경 정보도 정상 생성")
        void shouldCreateWithHtmlTags() {
            // Given
            String detailsWithHtml = "<strong>중요:</strong> 가격 변경 <em>10,000원</em> -> <em>12,000원</em>";

            // When
            ChangeData changeData = ChangeData.of(detailsWithHtml);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(detailsWithHtml);
        }

        @Test
        @DisplayName("여러 줄의 변경 정보도 정상 생성")
        void shouldCreateWithMultilineDetails() {
            // Given
            String multilineDetails = """
                변경 내역:
                - 가격: 10,000원 -> 12,000원
                - 재고: 100개 -> 50개
                - 상태: 판매중 -> 품절
                """;

            // When
            ChangeData changeData = ChangeData.of(multilineDetails);

            // Then
            assertThat(changeData).isNotNull();
            assertThat(changeData.getValue()).isEqualTo(multilineDetails);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("ChangeData는 불변 객체이다")
        void shouldBeImmutable() {
            // Given
            String originalDetails = "가격 변경: 10,000원 -> 12,000원";
            ChangeData changeData = ChangeData.of(originalDetails);

            // When
            String retrievedDetails = changeData.getValue();

            // Then
            assertThat(retrievedDetails).isEqualTo(originalDetails);
            assertThat(changeData.getValue()).isEqualTo(originalDetails); // 여러 번 호출해도 같은 값
        }
    }
}
