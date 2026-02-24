package com.ryuqq.crawlinghub.domain.execution.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("RetryableExecutionException 단위 테스트")
class RetryableExecutionExceptionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("메시지와 원인 예외로 생성한다")
        void createWithMessageAndCause() {
            // given
            String message = "DB connection failed";
            RuntimeException cause = new RuntimeException("Connection timeout");

            // when
            RetryableExecutionException exception = new RetryableExecutionException(message, cause);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("원인 예외의 메시지에 접근할 수 있다")
        void accessCauseMessage() {
            // given
            String causeMessage = "Timeout after 30s";
            RuntimeException cause = new RuntimeException(causeMessage);

            // when
            RetryableExecutionException exception =
                    new RetryableExecutionException("DB failed", cause);

            // then
            assertThat(exception.getCause().getMessage()).isEqualTo(causeMessage);
        }
    }
}
