package com.ryuqq.crawlinghub.domain.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.DomainExceptionFixture;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DomainException 테스트")
class DomainExceptionTest {

    /** 테스트용 ErrorCode 구현체 */
    enum TestErrorCode implements ErrorCode {
        USER_NOT_FOUND("USER-001", 404, "User not found"),
        USER_INVALID("USER-002", 400, "User with id {userId} not found"),
        USER_ERROR("USER-003", 400, "Invalid user"),
        TEST_ERROR("TEST-002", 500, "Test");

        private final String code;
        private final int httpStatus;
        private final String message;

        TestErrorCode(String code, int httpStatus, String message) {
            this.code = code;
            this.httpStatus = httpStatus;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public int getHttpStatus() {
            return httpStatus;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    @Test
    @DisplayName("코드와 메시지로 DomainException 생성 성공")
    void shouldCreateDomainExceptionWithCodeAndMessage() {
        // Given
        ErrorCode errorCode = TestErrorCode.USER_NOT_FOUND;
        String message = "User not found";

        // When
        DomainException exception = DomainExceptionFixture.aDomainException(errorCode, message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.code()).isEqualTo(errorCode.getCode());
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("코드, 메시지, 인자로 DomainException 생성 성공")
    void shouldCreateDomainExceptionWithCodeMessageAndArgs() {
        // Given
        ErrorCode errorCode = TestErrorCode.USER_INVALID;
        String message = "User with id {userId} not found";
        Map<String, Object> args = Map.of("userId", 123L);

        // When
        DomainException exception =
                DomainExceptionFixture.aDomainExceptionWithArgs(errorCode, message, args);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.code()).isEqualTo(errorCode.getCode());
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.args()).containsEntry("userId", 123L);
    }

    @Test
    @DisplayName("null args로 DomainException 생성 시 빈 Map 반환")
    void shouldCreateDomainExceptionWithNullArgs() {
        // Given
        ErrorCode errorCode = TestErrorCode.USER_ERROR;
        String message = "Invalid user";
        Map<String, Object> nullArgs = null;

        // When
        DomainException exception =
                DomainExceptionFixture.aDomainExceptionWithArgs(errorCode, message, nullArgs);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("DomainException은 RuntimeException을 상속")
    void shouldExtendRuntimeException() {
        // When
        DomainException exception = DomainExceptionFixture.aDomainException();

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("args Map은 불변")
    void shouldReturnImmutableArgs() {
        // Given
        ErrorCode errorCode = TestErrorCode.TEST_ERROR;
        Map<String, Object> mutableArgs = Map.of("key", "value");

        // When
        DomainException exception =
                DomainExceptionFixture.aDomainExceptionWithArgs(errorCode, "Test", mutableArgs);

        // Then
        assertThat(exception.args()).isUnmodifiable();
    }
}
