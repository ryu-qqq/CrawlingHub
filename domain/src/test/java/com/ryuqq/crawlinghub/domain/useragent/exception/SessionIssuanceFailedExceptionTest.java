package com.ryuqq.crawlinghub.domain.useragent.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("SessionIssuanceFailedException 단위 테스트")
class SessionIssuanceFailedExceptionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("userAgentId와 reason으로 예외를 생성한다")
        void createWithUserAgentIdAndReason() {
            // given
            Long userAgentId = 1L;
            String reason = "HTTP 401 Unauthorized";

            // when
            SessionIssuanceFailedException exception =
                    new SessionIssuanceFailedException(userAgentId, reason);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("메시지에 UserAgentId가 포함된다")
        void messageContainsUserAgentId() {
            // when
            SessionIssuanceFailedException exception =
                    new SessionIssuanceFailedException(77L, "Connection timeout");

            // then
            assertThat(exception.getMessage()).contains("77");
        }

        @Test
        @DisplayName("메시지에 reason이 포함된다")
        void messageContainsReason() {
            // when
            SessionIssuanceFailedException exception =
                    new SessionIssuanceFailedException(1L, "Network error occurred");

            // then
            assertThat(exception.getMessage()).contains("Network error occurred");
        }

        @Test
        @DisplayName("getUserAgentId()로 UserAgentId를 반환한다")
        void getUserAgentIdReturnsCorrectValue() {
            // given
            Long expectedId = 42L;

            // when
            SessionIssuanceFailedException exception =
                    new SessionIssuanceFailedException(expectedId, "reason");

            // then
            assertThat(exception.getUserAgentId()).isEqualTo(expectedId);
        }
    }

    @Nested
    @DisplayName("ErrorCode 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("에러 코드는 SESSION_ISSUANCE_FAILED이다")
        void errorCodeIsSessionIssuanceFailed() {
            // when
            SessionIssuanceFailedException exception =
                    new SessionIssuanceFailedException(1L, "failed");

            // then
            assertThat(exception.getErrorCode())
                    .isEqualTo(UserAgentErrorCode.SESSION_ISSUANCE_FAILED);
        }

        @Test
        @DisplayName("에러 코드의 HTTP 상태는 503이다")
        void errorCodeHttpStatusIs503() {
            // when
            SessionIssuanceFailedException exception =
                    new SessionIssuanceFailedException(1L, "failed");

            // then
            assertThat(exception.httpStatus()).isEqualTo(503);
        }

        @Test
        @DisplayName("에러 코드는 USER-AGENT-008이다")
        void errorCodeIsUserAgent008() {
            // when
            SessionIssuanceFailedException exception =
                    new SessionIssuanceFailedException(1L, "failed");

            // then
            assertThat(exception.code()).isEqualTo("USER-AGENT-008");
        }
    }
}
