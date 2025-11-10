package com.ryuqq.crawlinghub.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CompletionStatus 테스트")
class CompletionStatusTest {

    @Nested
    @DisplayName("Enum 값 테스트")
    class EnumValueTests {

        @Test
        @DisplayName("INCOMPLETE 상수 검증")
        void shouldHaveIncompleteStatus() {
            // Given & When
            CompletionStatus status = CompletionStatus.INCOMPLETE;

            // Then
            assertThat(status.getPriority()).isEqualTo(1);
            assertThat(status.getDescription()).isEqualTo("미완성");
        }

        @Test
        @DisplayName("COMPLETE 상수 검증")
        void shouldHaveCompleteStatus() {
            // Given & When
            CompletionStatus status = CompletionStatus.COMPLETE;

            // Then
            assertThat(status.getPriority()).isEqualTo(2);
            assertThat(status.getDescription()).isEqualTo("완성");
        }

        @ParameterizedTest
        @EnumSource(CompletionStatus.class)
        @DisplayName("모든 Enum 값이 priority와 description을 가진다")
        void shouldHavePriorityAndDescription(CompletionStatus status) {
            // Then
            assertThat(status.getPriority()).isGreaterThan(0);
            assertThat(status.getDescription()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("isComplete() 메서드 테스트")
    class IsCompleteTests {

        @Test
        @DisplayName("COMPLETE 상태는 true 반환")
        void shouldReturnTrueForComplete() {
            // Given
            CompletionStatus status = CompletionStatus.COMPLETE;

            // When
            boolean result = status.isComplete();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("INCOMPLETE 상태는 false 반환")
        void shouldReturnFalseForIncomplete() {
            // Given
            CompletionStatus status = CompletionStatus.INCOMPLETE;

            // When
            boolean result = status.isComplete();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("fromString() Static Factory 테스트")
    class FromStringTests {

        @Test
        @DisplayName("대문자 'COMPLETE' 문자열로 COMPLETE 상태 생성")
        void shouldCreateCompleteFromUpperCase() {
            // When
            CompletionStatus status = CompletionStatus.fromString("COMPLETE");

            // Then
            assertThat(status).isEqualTo(CompletionStatus.COMPLETE);
        }

        @Test
        @DisplayName("대문자 'INCOMPLETE' 문자열로 INCOMPLETE 상태 생성")
        void shouldCreateIncompleteFromUpperCase() {
            // When
            CompletionStatus status = CompletionStatus.fromString("INCOMPLETE");

            // Then
            assertThat(status).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("소문자 문자열은 대문자로 변환되어 생성")
        void shouldConvertLowerCaseToUpperCase() {
            // When
            CompletionStatus complete = CompletionStatus.fromString("complete");
            CompletionStatus incomplete = CompletionStatus.fromString("incomplete");

            // Then
            assertThat(complete).isEqualTo(CompletionStatus.COMPLETE);
            assertThat(incomplete).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("공백이 포함된 문자열은 trim되어 처리")
        void shouldTrimWhitespace() {
            // When
            CompletionStatus status = CompletionStatus.fromString("  COMPLETE  ");

            // Then
            assertThat(status).isEqualTo(CompletionStatus.COMPLETE);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 문자열은 예외 발생")
        void shouldThrowExceptionForNull(String nullStr) {
            // When & Then
            assertThatThrownBy(() -> CompletionStatus.fromString(nullStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CompletionStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("빈 문자열/공백 문자열은 예외 발생")
        void shouldThrowExceptionForBlank(String blankStr) {
            // When & Then
            assertThatThrownBy(() -> CompletionStatus.fromString(blankStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CompletionStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "unknown", "123", "com plete"})
        @DisplayName("유효하지 않은 문자열은 예외 발생")
        void shouldThrowExceptionForInvalidString(String invalidStr) {
            // When & Then
            assertThatThrownBy(() -> CompletionStatus.fromString(invalidStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 CompletionStatus입니다");
        }
    }

    @Nested
    @DisplayName("Enum 비교 테스트")
    class ComparisonTests {

        @Test
        @DisplayName("같은 Enum 값은 동일하다")
        void shouldBeEqualForSameEnum() {
            // Given
            CompletionStatus status1 = CompletionStatus.COMPLETE;
            CompletionStatus status2 = CompletionStatus.COMPLETE;

            // Then
            assertThat(status1).isEqualTo(status2);
            assertThat(status1).isSameAs(status2);  // Enum은 싱글톤
        }

        @Test
        @DisplayName("다른 Enum 값은 동일하지 않다")
        void shouldNotBeEqualForDifferentEnum() {
            // Given
            CompletionStatus status1 = CompletionStatus.COMPLETE;
            CompletionStatus status2 = CompletionStatus.INCOMPLETE;

            // Then
            assertThat(status1).isNotEqualTo(status2);
            assertThat(status1).isNotSameAs(status2);
        }

        @Test
        @DisplayName("Priority로 우선순위 비교 가능")
        void shouldComparePriority() {
            // Given
            CompletionStatus incomplete = CompletionStatus.INCOMPLETE;
            CompletionStatus complete = CompletionStatus.COMPLETE;

            // Then
            assertThat(incomplete.getPriority()).isLessThan(complete.getPriority());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("COMPLETE toString은 'COMPLETE' 반환")
        void shouldReturnCompleteAsString() {
            // Given
            CompletionStatus status = CompletionStatus.COMPLETE;

            // When
            String result = status.toString();

            // Then
            assertThat(result).isEqualTo("COMPLETE");
        }

        @Test
        @DisplayName("INCOMPLETE toString은 'INCOMPLETE' 반환")
        void shouldReturnIncompleteAsString() {
            // Given
            CompletionStatus status = CompletionStatus.INCOMPLETE;

            // When
            String result = status.toString();

            // Then
            assertThat(result).isEqualTo("INCOMPLETE");
        }
    }
}
