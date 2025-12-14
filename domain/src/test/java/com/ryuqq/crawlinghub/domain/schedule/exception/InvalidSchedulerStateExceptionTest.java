package com.ryuqq.crawlinghub.domain.schedule.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("InvalidSchedulerStateException 단위 테스트")
class InvalidSchedulerStateExceptionTest {

    @Nested
    @DisplayName("예외 생성")
    class ExceptionCreation {

        @Test
        @DisplayName("현재 상태와 기대 상태로 예외 생성")
        void shouldCreateWithCurrentAndExpectedStatus() {
            // Given
            SchedulerStatus currentStatus = SchedulerStatus.INACTIVE;
            SchedulerStatus expectedStatus = SchedulerStatus.ACTIVE;

            // When
            InvalidSchedulerStateException exception =
                    new InvalidSchedulerStateException(currentStatus, expectedStatus);

            // Then
            assertThat(exception.code()).isEqualTo("SCHEDULE-003");
            assertThat(exception.getMessage()).contains("유효하지 않습니다");
            assertThat(exception.getMessage()).contains("INACTIVE");
            assertThat(exception.getMessage()).contains("ACTIVE");
            assertThat(exception.args()).containsEntry("currentStatus", "INACTIVE");
            assertThat(exception.args()).containsEntry("expectedStatus", "ACTIVE");
        }

        @Test
        @DisplayName("ACTIVE에서 ACTIVE 상태 전이 시도 시 예외 생성")
        void shouldCreateForActiveToActiveTransition() {
            // Given
            SchedulerStatus currentStatus = SchedulerStatus.ACTIVE;
            SchedulerStatus expectedStatus = SchedulerStatus.INACTIVE;

            // When
            InvalidSchedulerStateException exception =
                    new InvalidSchedulerStateException(currentStatus, expectedStatus);

            // Then
            assertThat(exception.getMessage()).contains("ACTIVE");
            assertThat(exception.getMessage()).contains("INACTIVE");
            assertThat(exception.args()).containsEntry("currentStatus", "ACTIVE");
            assertThat(exception.args()).containsEntry("expectedStatus", "INACTIVE");
        }
    }

    @Nested
    @DisplayName("DomainException 상속 검증")
    class DomainExceptionInheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // Given
            InvalidSchedulerStateException exception =
                    new InvalidSchedulerStateException(
                            SchedulerStatus.ACTIVE, SchedulerStatus.INACTIVE);

            // When & Then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("HTTP 상태 코드 400을 반환한다")
        void shouldReturn400HttpStatus() {
            // Given
            InvalidSchedulerStateException exception =
                    new InvalidSchedulerStateException(
                            SchedulerStatus.ACTIVE, SchedulerStatus.INACTIVE);

            // When & Then
            assertThat(exception.httpStatus()).isEqualTo(400);
        }
    }
}
