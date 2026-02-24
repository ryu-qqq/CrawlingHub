package com.ryuqq.crawlinghub.adapter.in.rest.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * DateTimeFormatUtils 단위 테스트
 *
 * <p>날짜/시간 포맷 변환 유틸리티 클래스의 모든 메서드를 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("DateTimeFormatUtils 단위 테스트")
class DateTimeFormatUtilsTest {

    @Nested
    @DisplayName("인스턴스화 불가 검증")
    class InstantiationTest {

        @Test
        @DisplayName("생성자를 호출하면 UnsupportedOperationException이 발생한다")
        void shouldThrowExceptionWhenInstantiated() throws Exception {
            // 리플렉션을 사용하여 private 생성자 접근 시도
            var constructor = DateTimeFormatUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .cause()
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Utility class cannot be instantiated");
        }
    }

    @Nested
    @DisplayName("format(Instant) 메서드는")
    class FormatInstantTest {

        @Test
        @DisplayName("Instant를 'yyyy-MM-dd HH:mm:ss' 포맷으로 변환한다")
        void shouldFormatInstantToStandardPattern() {
            // Given
            // 2024-01-15T01:30:00Z → KST(+09:00)로 변환하면 2024-01-15 10:30:00
            Instant instant = Instant.parse("2024-01-15T01:30:00Z");

            // When
            String result = DateTimeFormatUtils.format(instant);

            // Then
            assertThat(result).isEqualTo("2024-01-15 10:30:00");
        }

        @Test
        @DisplayName("null Instant 입력 시 null을 반환한다")
        void shouldReturnNullWhenInstantIsNull() {
            // When
            String result = DateTimeFormatUtils.format((Instant) null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("반환 포맷이 yyyy-MM-dd HH:mm:ss 패턴을 따른다")
        void shouldFollowStandardDateTimePattern() {
            // Given
            Instant instant = Instant.now();

            // When
            String result = DateTimeFormatUtils.format(instant);

            // Then
            // 패턴: "2024-01-15 10:30:00" (날짜 10자 + 공백 + 시간 8자 = 19자)
            assertThat(result).isNotNull();
            assertThat(result).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        }
    }

    @Nested
    @DisplayName("format(LocalDateTime) 메서드는")
    class FormatLocalDateTimeTest {

        @Test
        @DisplayName("LocalDateTime을 'yyyy-MM-dd HH:mm:ss' 포맷으로 변환한다")
        void shouldFormatLocalDateTimeToStandardPattern() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            String result = DateTimeFormatUtils.format(localDateTime);

            // Then
            assertThat(result).isEqualTo("2024-01-15 10:30:00");
        }

        @Test
        @DisplayName("null LocalDateTime 입력 시 null을 반환한다")
        void shouldReturnNullWhenLocalDateTimeIsNull() {
            // When
            String result = DateTimeFormatUtils.format((LocalDateTime) null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("반환 포맷이 yyyy-MM-dd HH:mm:ss 패턴을 따른다")
        void shouldFollowStandardDateTimePattern() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now();

            // When
            String result = DateTimeFormatUtils.format(localDateTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        }
    }

    @Nested
    @DisplayName("formatIso8601(Instant) 메서드는")
    class FormatIso8601InstantTest {

        @Test
        @DisplayName("Instant를 ISO 8601 포맷으로 변환한다")
        void shouldFormatInstantToIso8601() {
            // Given
            // 2024-01-15T01:30:00Z → KST(+09:00)로 변환하면 2024-01-15T10:30:00+09:00
            Instant instant = Instant.parse("2024-01-15T01:30:00Z");

            // When
            String result = DateTimeFormatUtils.formatIso8601(instant);

            // Then
            assertThat(result).isEqualTo("2024-01-15T10:30:00+09:00");
        }

        @Test
        @DisplayName("null Instant 입력 시 null을 반환한다")
        void shouldReturnNullWhenInstantIsNull() {
            // When
            String result = DateTimeFormatUtils.formatIso8601((Instant) null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("타임존 오프셋 +09:00을 포함한다")
        void shouldIncludeKstTimezoneOffset() {
            // Given
            Instant instant = Instant.now();

            // When
            String result = DateTimeFormatUtils.formatIso8601(instant);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("+09:00");
        }

        @Test
        @DisplayName("ISO 8601 날짜시간 패턴을 따른다")
        void shouldFollowIso8601Pattern() {
            // Given
            Instant instant = Instant.now();

            // When
            String result = DateTimeFormatUtils.formatIso8601(instant);

            // Then
            // 패턴: "2024-01-15T10:30:00+09:00" 또는 나노초 포함 "2024-01-15T10:30:00.123456+09:00"
            assertThat(result).isNotNull();
            assertThat(result)
                    .matches(
                            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?[+-]\\d{2}:\\d{2}");
        }
    }

    @Nested
    @DisplayName("formatIso8601(LocalDateTime) 메서드는")
    class FormatIso8601LocalDateTimeTest {

        @Test
        @DisplayName("LocalDateTime을 ISO 8601 포맷으로 변환한다")
        void shouldFormatLocalDateTimeToIso8601() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            String result = DateTimeFormatUtils.formatIso8601(localDateTime);

            // Then
            assertThat(result).isEqualTo("2024-01-15T10:30:00+09:00");
        }

        @Test
        @DisplayName("null LocalDateTime 입력 시 null을 반환한다")
        void shouldReturnNullWhenLocalDateTimeIsNull() {
            // When
            String result = DateTimeFormatUtils.formatIso8601((LocalDateTime) null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("타임존 오프셋 +09:00을 포함한다")
        void shouldIncludeKstTimezoneOffset() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            // When
            String result = DateTimeFormatUtils.formatIso8601(localDateTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("+09:00");
        }
    }

    @Nested
    @DisplayName("nowIso8601() 메서드는")
    class NowIso8601Test {

        @Test
        @DisplayName("현재 시간을 ISO 8601 포맷으로 반환한다")
        void shouldReturnCurrentTimeInIso8601Format() {
            // When
            String result = DateTimeFormatUtils.nowIso8601();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isNotBlank();
        }

        @Test
        @DisplayName("ISO 8601 패턴을 따른다")
        void shouldFollowIso8601Pattern() {
            // When
            String result = DateTimeFormatUtils.nowIso8601();

            // Then
            // 나노초가 포함될 수 있으므로 유연한 패턴 사용
            assertThat(result)
                    .matches(
                            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?[+-]\\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("KST 타임존 오프셋 +09:00을 포함한다")
        void shouldIncludeKstTimezoneOffset() {
            // When
            String result = DateTimeFormatUtils.nowIso8601();

            // Then
            assertThat(result).contains("+09:00");
        }
    }
}
