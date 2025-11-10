package com.ryuqq.crawlinghub.domain.schedule.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScheduleErrorCode 테스트")
class ScheduleErrorCodeTest {

    @Nested
    @DisplayName("Enum 값 테스트")
    class EnumValueTests {

        @Test
        @DisplayName("SCHEDULE_PLACEHOLDER 상수 존재")
        void shouldHaveSchedulePlaceholder() {
            // Given & When
            ScheduleErrorCode errorCode = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("SCHEDULE_PLACEHOLDER");
        }

        @Test
        @DisplayName("Enum 값은 정확히 1개")
        void shouldHaveExactlyOneValue() {
            // When
            ScheduleErrorCode[] values = ScheduleErrorCode.values();

            // Then
            assertThat(values).hasSize(1);
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 Enum 값이 유효")
        void shouldHaveValidValues(ScheduleErrorCode errorCode) {
            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("getCode() 메서드 테스트")
    class GetCodeTests {

        @Test
        @DisplayName("SCHEDULE_PLACEHOLDER는 'SCHEDULE-001' 반환")
        void shouldReturnSchedulePlaceholderCode() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("SCHEDULE-001");
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 Error Code는 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyCode(ScheduleErrorCode errorCode) {
            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 Error Code는 'SCHEDULE-' 접두사로 시작")
        void shouldStartWithSchedulePrefix(ScheduleErrorCode errorCode) {
            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).startsWith("SCHEDULE-");
        }
    }

    @Nested
    @DisplayName("getHttpStatus() 메서드 테스트")
    class GetHttpStatusTests {

        @Test
        @DisplayName("SCHEDULE_PLACEHOLDER는 404 반환")
        void shouldReturn404ForPlaceholder() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(404);
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 HTTP 상태 코드는 유효한 범위 (400-599)")
        void shouldHaveValidHttpStatus(ScheduleErrorCode errorCode) {
            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isBetween(400, 599);
        }
    }

    @Nested
    @DisplayName("getMessage() 메서드 테스트")
    class GetMessageTests {

        @Test
        @DisplayName("SCHEDULE_PLACEHOLDER는 'Placeholder exception' 반환")
        void shouldReturnPlaceholderMessage() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("Placeholder exception");
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 메시지는 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyMessage(ScheduleErrorCode errorCode) {
            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isNotNull().isNotBlank();
        }
    }

    @Nested
    @DisplayName("getTitle() 메서드 테스트")
    class GetTitleTests {

        @Test
        @DisplayName("SCHEDULE_PLACEHOLDER는 'Schedule Placeholder' 반환")
        void shouldReturnPlaceholderTitle() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("Schedule Placeholder");
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 타이틀은 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyTitle(ScheduleErrorCode errorCode) {
            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isNotNull().isNotBlank();
        }
    }

    @Nested
    @DisplayName("valueOf() 메서드 테스트")
    class ValueOfTests {

        @Test
        @DisplayName("문자열 'SCHEDULE_PLACEHOLDER'로 Enum 생성")
        void shouldCreateFromPlaceholderString() {
            // When
            ScheduleErrorCode errorCode = ScheduleErrorCode.valueOf("SCHEDULE_PLACEHOLDER");

            // Then
            assertThat(errorCode).isEqualTo(ScheduleErrorCode.SCHEDULE_PLACEHOLDER);
        }
    }

    @Nested
    @DisplayName("Enum 비교 테스트")
    class ComparisonTests {

        @Test
        @DisplayName("같은 Enum 값은 동일하다")
        void shouldBeEqualForSameEnum() {
            // Given
            ScheduleErrorCode errorCode1 = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;
            ScheduleErrorCode errorCode2 = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // Then
            assertThat(errorCode1).isEqualTo(errorCode2);
            assertThat(errorCode1).isSameAs(errorCode2);  // Enum은 싱글톤
        }

        @Test
        @DisplayName("Enum 순서 확인")
        void shouldHaveCorrectOrdinal() {
            // Given
            ScheduleErrorCode[] values = ScheduleErrorCode.values();

            // Then
            assertThat(values[0]).isEqualTo(ScheduleErrorCode.SCHEDULE_PLACEHOLDER);
            assertThat(ScheduleErrorCode.SCHEDULE_PLACEHOLDER.ordinal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("SCHEDULE_PLACEHOLDER toString은 'SCHEDULE_PLACEHOLDER' 반환")
        void shouldReturnPlaceholderAsString() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("SCHEDULE_PLACEHOLDER");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("404 Not Found 에러 코드 사용 시나리오")
        void shouldHandleNotFoundScenario() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.SCHEDULE_PLACEHOLDER;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SCHEDULE-001");
            assertThat(errorCode.getHttpStatus()).isEqualTo(404);
            assertThat(errorCode.getMessage()).isEqualTo("Placeholder exception");
            assertThat(errorCode.getTitle()).isEqualTo("Schedule Placeholder");
        }

        @Test
        @DisplayName("Switch 문에서 모든 에러 코드 처리 가능")
        void shouldHandleAllErrorCodesInSwitch() {
            // Given
            ScheduleErrorCode[] allErrorCodes = ScheduleErrorCode.values();

            // When & Then
            for (ScheduleErrorCode errorCode : allErrorCodes) {
                String result = switch (errorCode) {
                    case SCHEDULE_PLACEHOLDER -> "Placeholder";
                };
                assertThat(result).isNotBlank();
            }
        }

        @Test
        @DisplayName("HTTP 상태 코드별 그룹화 가능")
        void shouldGroupByHttpStatus() {
            // Given
            ScheduleErrorCode[] allErrorCodes = ScheduleErrorCode.values();

            // When
            long notFoundCount = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getHttpStatus() == 404)
                .count();

            // Then
            assertThat(notFoundCount).isEqualTo(1);  // SCHEDULE_PLACEHOLDER만
        }
    }
}
