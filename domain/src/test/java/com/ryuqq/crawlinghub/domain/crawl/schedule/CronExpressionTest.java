package com.ryuqq.crawlinghub.domain.crawl.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CronExpression Value Object 단위 테스트
 * Quartz Cron 형식 (6-7 필드: 초 분 시 일 월 요일 [년도])
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("CronExpression Value Object 단위 테스트")
class CronExpressionTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "0 0 * * * ?",          // 매시 정각
            "0 0 0 * * ?",          // 매일 자정
            "0 0 12 * * ?",         // 매일 정오
            "0 0 0 * * MON",        // 매주 월요일 자정
            "0 0 0 1 * ?",          // 매월 1일 자정
            "0 0 0 1 1 ?",          // 매년 1월 1일 자정
            "0 0 9-17 * * MON-FRI", // 주중 9시~17시
            "0 0 0,12 * * ?",       // 자정과 정오
            "* * * * * ?",          // 매초
            "0 * * * * ?"           // 매분
        })
        @DisplayName("유효한 Quartz Cron 표현식으로 CronExpression 생성 성공")
        void shouldCreateWithValidCronExpression(String validExpression) {
            // When
            CronExpression cronExpression = CronExpression.of(validExpression);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(validExpression);
        }

        @Test
        @DisplayName("앞뒤 공백이 있는 Cron 표현식도 정상 생성")
        void shouldCreateWithWhitespace() {
            // Given
            String expressionWithWhitespace = "  0 0 * * * ?  ";

            // When
            CronExpression cronExpression = CronExpression.of(expressionWithWhitespace);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(expressionWithWhitespace);
        }

        @Test
        @DisplayName("월 이름으로 된 Cron 표현식도 정상 생성")
        void shouldCreateWithMonthNames() {
            // Given
            String expressionWithMonths = "0 0 0 1 JAN,MAR,SEP ?";

            // When
            CronExpression cronExpression = CronExpression.of(expressionWithMonths);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(expressionWithMonths);
        }

        @Test
        @DisplayName("요일 이름으로 된 Cron 표현식도 정상 생성")
        void shouldCreateWithDayNames() {
            // Given
            String expressionWithDays = "0 0 0 ? * MON-FRI";

            // When
            CronExpression cronExpression = CronExpression.of(expressionWithDays);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(expressionWithDays);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Cron 표현식이 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenNullOrBlank(String invalidExpression) {
            // When & Then
            assertThatThrownBy(() -> CronExpression.of(invalidExpression))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid cron",
            "60 * * * * ?",       // 분이 60 (범위 초과)
            "* 60 * * * ?",       // 시간이 60 (범위 초과)
            "* * 25 * * ?",       // 시간이 25 (범위 초과)
            "* * * * * * * *",    // 필드 너무 많음
            "* * *",              // 필드 너무 적음
            "0 0 0 32 * ?",       // 일이 32 (범위 초과)
            "0 0 0 * 13 ?",       // 월이 13 (범위 초과)
            "0 0 0 ? * 8"         // 요일이 8 (범위 초과)
        })
        @DisplayName("잘못된 Cron 표현식 형식이면 예외 발생")
        void shouldThrowExceptionWhenInvalidCronFormat(String invalidExpression) {
            // When & Then
            assertThatThrownBy(() -> CronExpression.of(invalidExpression))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 Cron 표현식입니다");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 표현식을 가진 두 CronExpression은 isSameAs() 가 true 반환")
        void shouldReturnTrueForSameExpression() {
            // Given
            String expression = "0 0 * * * ?";
            CronExpression expr1 = CronExpression.of(expression);
            CronExpression expr2 = CronExpression.of(expression);

            // When
            boolean result = expr1.isSameAs(expr2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 표현식을 가진 두 CronExpression은 isSameAs() 가 false 반환")
        void shouldReturnFalseForDifferentExpression() {
            // Given
            CronExpression expr1 = CronExpression.of("0 0 * * * ?");
            CronExpression expr2 = CronExpression.of("0 0 12 * * ?");

            // When
            boolean result = expr1.isSameAs(expr2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null과 비교하면 isSameAs() 가 false 반환")
        void shouldReturnFalseWhenComparedWithNull() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * ?");

            // When
            boolean result = cronExpression.isSameAs(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("같은 표현식을 가진 두 CronExpression은 equals() 가 true 반환")
        void shouldReturnTrueForEquals() {
            // Given
            String expression = "0 0 * * * ?";
            CronExpression expr1 = CronExpression.of(expression);
            CronExpression expr2 = CronExpression.of(expression);

            // When & Then
            assertThat(expr1).isEqualTo(expr2);
        }

        @Test
        @DisplayName("같은 표현식을 가진 두 CronExpression은 같은 hashCode 반환")
        void shouldReturnSameHashCode() {
            // Given
            String expression = "0 0 * * * ?";
            CronExpression expr1 = CronExpression.of(expression);
            CronExpression expr2 = CronExpression.of(expression);

            // When & Then
            assertThat(expr1.hashCode()).isEqualTo(expr2.hashCode());
        }

        @Test
        @DisplayName("다른 표현식을 가진 두 CronExpression은 다른 hashCode 반환")
        void shouldReturnDifferentHashCode() {
            // Given
            CronExpression expr1 = CronExpression.of("0 0 * * * ?");
            CronExpression expr2 = CronExpression.of("0 0 12 * * ?");

            // When & Then
            assertThat(expr1.hashCode()).isNotEqualTo(expr2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 Cron 표현식 문자열 반환")
        void shouldReturnExpressionAsString() {
            // Given
            String expression = "0 0 * * * ?";
            CronExpression cronExpression = CronExpression.of(expression);

            // When
            String result = cronExpression.toString();

            // Then
            assertThat(result).isEqualTo(expression);
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("매우 복잡한 Cron 표현식도 정상 생성")
        void shouldCreateComplexCronExpression() {
            // Given
            String complexExpression = "0 0 0 1-7 JAN,MAR MON-FRI";

            // When
            CronExpression cronExpression = CronExpression.of(complexExpression);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(complexExpression);
        }

        @Test
        @DisplayName("와일드카드만 있는 표현식도 정상 생성")
        void shouldCreateWithOnlyWildcards() {
            // Given
            String wildcardExpression = "* * * * * ?";

            // When
            CronExpression cronExpression = CronExpression.of(wildcardExpression);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(wildcardExpression);
        }

        @Test
        @DisplayName("년도 필드가 있는 7필드 Cron 표현식도 정상 생성")
        void shouldCreateWithYearField() {
            // Given
            String expressionWithYear = "0 0 0 1 1 ? 2025";

            // When
            CronExpression cronExpression = CronExpression.of(expressionWithYear);

            // Then
            assertThat(cronExpression).isNotNull();
            assertThat(cronExpression.getValue()).isEqualTo(expressionWithYear);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("CronExpression은 불변 객체이다")
        void shouldBeImmutable() {
            // Given
            String originalExpression = "0 0 * * * ?";
            CronExpression cronExpression = CronExpression.of(originalExpression);

            // When
            String retrievedExpression = cronExpression.getValue();

            // Then
            assertThat(retrievedExpression).isEqualTo(originalExpression);
            assertThat(cronExpression.getValue()).isEqualTo(originalExpression); // 여러 번 호출해도 같은 값
        }
    }
}
