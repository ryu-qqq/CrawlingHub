package com.ryuqq.crawlinghub.application.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * StringTruncator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("StringTruncator 테스트")
class StringTruncatorTest {

    @Nested
    @DisplayName("truncate(String) 테스트")
    class TruncateDefault {

        @Test
        @DisplayName("[성공] null 입력 시 'null' 문자열 반환")
        void shouldReturnNullStringWhenInputIsNull() {
            // When
            String result = StringTruncator.truncate(null);

            // Then
            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("[성공] 200자 이하 문자열은 그대로 반환")
        void shouldReturnOriginalWhenLengthIsLessThanDefault() {
            // Given
            String input = "Short string";

            // When
            String result = StringTruncator.truncate(input);

            // Then
            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("[성공] 정확히 200자 문자열은 그대로 반환")
        void shouldReturnOriginalWhenLengthIsExactlyDefault() {
            // Given
            String input = "a".repeat(200);

            // When
            String result = StringTruncator.truncate(input);

            // Then
            assertThat(result).isEqualTo(input);
            assertThat(result).hasSize(200);
        }

        @Test
        @DisplayName("[성공] 200자 초과 문자열은 잘림 + ellipsis")
        void shouldTruncateWhenExceedsDefault() {
            // Given
            String input = "a".repeat(250);

            // When
            String result = StringTruncator.truncate(input);

            // Then
            assertThat(result).hasSize(203); // 200 + "..."
            assertThat(result).endsWith("...");
            assertThat(result).startsWith("a".repeat(200));
        }
    }

    @Nested
    @DisplayName("truncate(String, int) 테스트")
    class TruncateCustomLength {

        @Test
        @DisplayName("[성공] null 입력 시 'null' 문자열 반환")
        void shouldReturnNullStringWhenInputIsNull() {
            // When
            String result = StringTruncator.truncate(null, 50);

            // Then
            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("[성공] 지정 길이 이하 문자열은 그대로 반환")
        void shouldReturnOriginalWhenWithinLimit() {
            // Given
            String input = "Hello World";

            // When
            String result = StringTruncator.truncate(input, 50);

            // Then
            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("[성공] 지정 길이 초과 시 잘림 + ellipsis")
        void shouldTruncateWhenExceedsLimit() {
            // Given
            String input = "Hello World, this is a long string";

            // When
            String result = StringTruncator.truncate(input, 10);

            // Then
            assertThat(result).isEqualTo("Hello Worl...");
            assertThat(result).hasSize(13); // 10 + "..."
        }

        @Test
        @DisplayName("[성공] 빈 문자열은 그대로 반환")
        void shouldHandleEmptyString() {
            // When
            String result = StringTruncator.truncate("", 10);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
