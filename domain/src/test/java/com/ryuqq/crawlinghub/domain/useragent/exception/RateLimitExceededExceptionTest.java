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
@DisplayName("RateLimitExceededException 단위 테스트")
class RateLimitExceededExceptionTest {

    @Nested
    @DisplayName("UserAgentId 기반 생성자 테스트")
    class UserAgentIdConstructorTest {

        @Test
        @DisplayName("UserAgentId로 예외를 생성한다")
        void createWithUserAgentId() {
            // given
            UserAgentId userAgentId = UserAgentIdFixture.anAssignedId();

            // when
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("메시지에 UserAgentId 값이 포함된다")
        void messageContainsUserAgentIdValue() {
            // given
            UserAgentId userAgentId = UserAgentId.of(99L);

            // when
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId);

            // then
            assertThat(exception.getMessage()).contains("99");
        }
    }

    @Nested
    @DisplayName("메시지 기반 생성자 테스트")
    class MessageConstructorTest {

        @Test
        @DisplayName("커스텀 메시지로 예외를 생성한다")
        void createWithCustomMessage() {
            // given
            String message = "Rate Limit exceeded for user agent pool";

            // when
            RateLimitExceededException exception = new RateLimitExceededException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("ErrorCode 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("에러 코드는 RATE_LIMIT_EXCEEDED이다")
        void errorCodeIsRateLimitExceeded() {
            // given
            UserAgentId userAgentId = UserAgentIdFixture.anAssignedId();

            // when
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId);

            // then
            assertThat(exception.getErrorCode()).isEqualTo(UserAgentErrorCode.RATE_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("에러 코드의 HTTP 상태는 429이다")
        void errorCodeHttpStatusIs429() {
            // when
            RateLimitExceededException exception = new RateLimitExceededException("test message");

            // then
            assertThat(exception.httpStatus()).isEqualTo(429);
        }

        @Test
        @DisplayName("에러 코드는 USER-AGENT-004이다")
        void errorCodeIsUserAgent004() {
            // when
            RateLimitExceededException exception =
                    new RateLimitExceededException(UserAgentIdFixture.anAssignedId());

            // then
            assertThat(exception.code()).isEqualTo("USER-AGENT-004");
        }
    }
}
