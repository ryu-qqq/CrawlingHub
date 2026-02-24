package com.ryuqq.crawlinghub.adapter.out.eventbridge.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * EventBridgePublishException 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("EventBridgePublishException 테스트")
class EventBridgePublishExceptionTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("메시지만으로 예외를 생성할 수 있다")
        void constructor_withMessage_createsException() {
            // given
            String message = "EventBridge 발행 실패";

            // when
            EventBridgePublishException exception = new EventBridgePublishException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("메시지와 원인으로 예외를 생성할 수 있다")
        void constructor_withMessageAndCause_createsException() {
            // given
            String message = "EventBridge 발행 실패";
            Throwable cause = new RuntimeException("AWS SDK 오류");

            // when
            EventBridgePublishException exception = new EventBridgePublishException(message, cause);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("RuntimeException을 상속한다")
        void exception_isRuntimeException() {
            // when
            EventBridgePublishException exception = new EventBridgePublishException("오류");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
