package com.ryuqq.crawlinghub.domain.useragent.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("CircuitBreakerOpenException 단위 테스트")
class CircuitBreakerOpenExceptionTest {

    @Nested
    @DisplayName("가용률 기반 생성자 테스트")
    class AvailableRateConstructorTest {

        @Test
        @DisplayName("가용률로 예외를 생성한다")
        void createWithAvailableRate() {
            // when
            CircuitBreakerOpenException exception = new CircuitBreakerOpenException(25.5);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("메시지에 가용률이 포함된다")
        void messageContainsAvailableRate() {
            // when
            CircuitBreakerOpenException exception = new CircuitBreakerOpenException(33.33);

            // then
            assertThat(exception.getMessage()).contains("33.33");
        }

        @Test
        @DisplayName("메시지에 Circuit Breaker OPEN이 포함된다")
        void messageContainsCircuitBreakerOpen() {
            // when
            CircuitBreakerOpenException exception = new CircuitBreakerOpenException(0.0);

            // then
            assertThat(exception.getMessage()).contains("Circuit Breaker");
        }
    }

    @Nested
    @DisplayName("메시지 기반 생성자 테스트")
    class MessageConstructorTest {

        @Test
        @DisplayName("커스텀 메시지로 예외를 생성한다")
        void createWithCustomMessage() {
            // given
            String message = "Circuit Breaker is open due to high failure rate";

            // when
            CircuitBreakerOpenException exception = new CircuitBreakerOpenException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("ErrorCode 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("에러 코드는 CIRCUIT_BREAKER_OPEN이다")
        void errorCodeIsCircuitBreakerOpen() {
            // when
            CircuitBreakerOpenException exception = new CircuitBreakerOpenException(10.0);

            // then
            assertThat(exception.getErrorCode()).isEqualTo(UserAgentErrorCode.CIRCUIT_BREAKER_OPEN);
        }

        @Test
        @DisplayName("에러 코드의 HTTP 상태는 503이다")
        void errorCodeHttpStatusIs503() {
            // when
            CircuitBreakerOpenException exception = new CircuitBreakerOpenException("test");

            // then
            assertThat(exception.httpStatus()).isEqualTo(503);
        }

        @Test
        @DisplayName("에러 코드는 USER-AGENT-005이다")
        void errorCodeIsUserAgent005() {
            // when
            CircuitBreakerOpenException exception = new CircuitBreakerOpenException(10.0);

            // then
            assertThat(exception.code()).isEqualTo("USER-AGENT-005");
        }
    }
}
