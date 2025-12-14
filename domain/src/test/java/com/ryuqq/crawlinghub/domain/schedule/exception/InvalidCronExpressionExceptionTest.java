package com.ryuqq.crawlinghub.domain.schedule.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("InvalidCronExpressionException 단위 테스트")
class InvalidCronExpressionExceptionTest {

    @Nested
    @DisplayName("dueToInvalidFormat 팩토리 메서드")
    class DueToInvalidFormatMethod {

        @Test
        @DisplayName("유효하지 않은 형식으로 예외 생성")
        void shouldCreateForInvalidFormat() {
            // Given
            String expression = "invalid-cron";

            // When
            InvalidCronExpressionException exception =
                    InvalidCronExpressionException.dueToInvalidFormat(expression);

            // Then
            assertThat(exception.code()).isEqualTo("SCHEDULE-004");
            assertThat(exception.getMessage()).contains("AWS EventBridge format");
            assertThat(exception.getMessage()).contains("invalid-cron");
            assertThat(exception.args()).containsEntry("expression", expression);
        }

        @Test
        @DisplayName("다른 유효하지 않은 형식으로 예외 생성")
        void shouldCreateForAnotherInvalidFormat() {
            // Given
            String expression = "* * * * *";

            // When
            InvalidCronExpressionException exception =
                    InvalidCronExpressionException.dueToInvalidFormat(expression);

            // Then
            assertThat(exception.getMessage()).contains("* * * * *");
            assertThat(exception.args()).containsEntry("expression", expression);
        }
    }

    @Nested
    @DisplayName("dueToInsufficientInterval 팩토리 메서드")
    class DueToInsufficientIntervalMethod {

        @Test
        @DisplayName("간격 부족으로 예외 생성")
        void shouldCreateForInsufficientInterval() {
            // Given
            String expression = "cron(*/5 * * * ? *)";

            // When
            InvalidCronExpressionException exception =
                    InvalidCronExpressionException.dueToInsufficientInterval(expression);

            // Then
            assertThat(exception.code()).isEqualTo("SCHEDULE-004");
            assertThat(exception.getMessage()).contains("at least 1 hour");
            assertThat(exception.getMessage()).contains("cron(*/5 * * * ? *)");
            assertThat(exception.args()).containsEntry("expression", expression);
        }

        @Test
        @DisplayName("다른 간격 부족 표현식으로 예외 생성")
        void shouldCreateForAnotherInsufficientInterval() {
            // Given
            String expression = "cron(0 */30 * * ? *)";

            // When
            InvalidCronExpressionException exception =
                    InvalidCronExpressionException.dueToInsufficientInterval(expression);

            // Then
            assertThat(exception.getMessage()).contains("cron(0 */30 * * ? *)");
            assertThat(exception.args()).containsEntry("expression", expression);
        }
    }

    @Nested
    @DisplayName("DomainException 상속 검증")
    class DomainExceptionInheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // Given
            InvalidCronExpressionException exception =
                    InvalidCronExpressionException.dueToInvalidFormat("test");

            // When & Then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("HTTP 상태 코드 400을 반환한다")
        void shouldReturn400HttpStatus() {
            // Given
            InvalidCronExpressionException exception =
                    InvalidCronExpressionException.dueToInvalidFormat("test");

            // When & Then
            assertThat(exception.httpStatus()).isEqualTo(400);
        }
    }
}
