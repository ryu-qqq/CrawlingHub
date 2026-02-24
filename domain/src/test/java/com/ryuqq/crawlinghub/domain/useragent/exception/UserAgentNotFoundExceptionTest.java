package com.ryuqq.crawlinghub.domain.useragent.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("UserAgentNotFoundException 단위 테스트")
class UserAgentNotFoundExceptionTest {

    @Nested
    @DisplayName("UserAgentId 기반 생성자 테스트")
    class UserAgentIdConstructorTest {

        @Test
        @DisplayName("UserAgentId로 예외를 생성한다")
        void createWithUserAgentId() {
            // given
            UserAgentId userAgentId = UserAgentIdFixture.anAssignedId();

            // when
            UserAgentNotFoundException exception = new UserAgentNotFoundException(userAgentId);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("메시지에 UserAgentId 값이 포함된다")
        void messageContainsUserAgentIdValue() {
            // given
            UserAgentId userAgentId = UserAgentId.of(42L);

            // when
            UserAgentNotFoundException exception = new UserAgentNotFoundException(userAgentId);

            // then
            assertThat(exception.getMessage()).contains("42");
        }
    }

    @Nested
    @DisplayName("메시지 기반 생성자 테스트")
    class MessageConstructorTest {

        @Test
        @DisplayName("커스텀 메시지로 예외를 생성한다")
        void createWithCustomMessage() {
            // given
            String message = "No UserAgent found with given criteria";

            // when
            UserAgentNotFoundException exception = new UserAgentNotFoundException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("ErrorCode 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("에러 코드는 USER_AGENT_NOT_FOUND이다")
        void errorCodeIsUserAgentNotFound() {
            // given
            UserAgentId userAgentId = UserAgentIdFixture.anAssignedId();

            // when
            UserAgentNotFoundException exception = new UserAgentNotFoundException(userAgentId);

            // then
            assertThat(exception.getErrorCode()).isEqualTo(UserAgentErrorCode.USER_AGENT_NOT_FOUND);
        }

        @Test
        @DisplayName("에러 코드의 HTTP 상태는 404이다")
        void errorCodeHttpStatusIs404() {
            // when
            UserAgentNotFoundException exception = new UserAgentNotFoundException("test message");

            // then
            assertThat(exception.httpStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("에러 코드는 USER-AGENT-001이다")
        void errorCodeIsUserAgent001() {
            // when
            UserAgentNotFoundException exception =
                    new UserAgentNotFoundException(UserAgentIdFixture.anAssignedId());

            // then
            assertThat(exception.code()).isEqualTo("USER-AGENT-001");
        }
    }
}
