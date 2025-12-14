package com.ryuqq.crawlinghub.domain.schedule.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("ScheduleErrorCode 단위 테스트")
class ScheduleErrorCodeTest {

    @Nested
    @DisplayName("ErrorCode 구현 검증")
    class ErrorCodeImplementation {

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 ErrorCode는 code를 가진다")
        void shouldHaveCodeForAllEnums(ScheduleErrorCode errorCode) {
            // When & Then
            assertThat(errorCode.getCode()).isNotBlank();
            assertThat(errorCode.getCode()).startsWith("SCHEDULE-");
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 ErrorCode는 유효한 HTTP 상태 코드를 가진다")
        void shouldHaveValidHttpStatusForAllEnums(ScheduleErrorCode errorCode) {
            // When & Then
            assertThat(errorCode.getHttpStatus()).isBetween(400, 599);
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 ErrorCode는 메시지를 가진다")
        void shouldHaveMessageForAllEnums(ScheduleErrorCode errorCode) {
            // When & Then
            assertThat(errorCode.getMessage()).isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(ScheduleErrorCode.class)
        @DisplayName("모든 ScheduleErrorCode는 ErrorCode 인터페이스를 구현한다")
        void shouldImplementErrorCodeInterface(ScheduleErrorCode errorCode) {
            // When & Then
            assertThat(errorCode).isInstanceOf(ErrorCode.class);
        }
    }

    @Nested
    @DisplayName("개별 ErrorCode 값 검증")
    class IndividualErrorCodeValues {

        @Test
        @DisplayName("CRAWL_SCHEDULER_NOT_FOUND는 올바른 값을 가진다")
        void shouldHaveCorrectValuesForNotFound() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.CRAWL_SCHEDULER_NOT_FOUND;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SCHEDULE-001");
            assertThat(errorCode.getHttpStatus()).isEqualTo(404);
            assertThat(errorCode.getMessage()).contains("존재하지 않는");
        }

        @Test
        @DisplayName("DUPLICATE_SCHEDULER_NAME은 올바른 값을 가진다")
        void shouldHaveCorrectValuesForDuplicateName() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.DUPLICATE_SCHEDULER_NAME;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SCHEDULE-002");
            assertThat(errorCode.getHttpStatus()).isEqualTo(409);
            assertThat(errorCode.getMessage()).contains("이미 존재하는");
        }

        @Test
        @DisplayName("INVALID_SCHEDULER_STATE는 올바른 값을 가진다")
        void shouldHaveCorrectValuesForInvalidState() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.INVALID_SCHEDULER_STATE;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SCHEDULE-003");
            assertThat(errorCode.getHttpStatus()).isEqualTo(400);
            assertThat(errorCode.getMessage()).contains("유효하지 않");
        }

        @Test
        @DisplayName("INVALID_CRON_EXPRESSION은 올바른 값을 가진다")
        void shouldHaveCorrectValuesForInvalidCron() {
            // Given
            ScheduleErrorCode errorCode = ScheduleErrorCode.INVALID_CRON_EXPRESSION;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SCHEDULE-004");
            assertThat(errorCode.getHttpStatus()).isEqualTo(400);
            assertThat(errorCode.getMessage()).contains("Cron");
        }
    }

    @Nested
    @DisplayName("ErrorCode 고유성 검증")
    class ErrorCodeUniqueness {

        @Test
        @DisplayName("모든 ErrorCode는 고유한 code를 가진다")
        void shouldHaveUniqueCodeForAllEnums() {
            // Given
            ScheduleErrorCode[] errorCodes = ScheduleErrorCode.values();

            // When & Then
            long uniqueCount =
                    java.util.Arrays.stream(errorCodes)
                            .map(ScheduleErrorCode::getCode)
                            .distinct()
                            .count();

            assertThat(uniqueCount).isEqualTo(errorCodes.length);
        }
    }
}
