package com.ryuqq.crawlinghub.domain.useragent.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("UserAgentErrorCode 단위 테스트")
class UserAgentErrorCodeTest {

    @Nested
    @DisplayName("enum 값 존재 테스트")
    class EnumValueTest {

        @Test
        @DisplayName("총 8개의 에러 코드가 존재한다")
        void hasEightValues() {
            assertThat(UserAgentErrorCode.values()).hasSize(8);
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 에러 코드의 code는 null이 아니다")
        void allCodesAreNotNull(UserAgentErrorCode errorCode) {
            assertThat(errorCode.getCode()).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 에러 코드의 message는 null이 아니다")
        void allMessagesAreNotNull(UserAgentErrorCode errorCode) {
            assertThat(errorCode.getMessage()).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 에러 코드의 httpStatus는 양수이다")
        void allHttpStatusesArePositive(UserAgentErrorCode errorCode) {
            assertThat(errorCode.getHttpStatus()).isPositive();
        }
    }

    @Nested
    @DisplayName("에러 코드 상세 테스트")
    class ErrorCodeDetailTest {

        @Test
        @DisplayName("USER_AGENT_NOT_FOUND - 코드는 USER-AGENT-001이고 상태는 404이다")
        void userAgentNotFoundCode() {
            assertThat(UserAgentErrorCode.USER_AGENT_NOT_FOUND.getCode())
                    .isEqualTo("USER-AGENT-001");
            assertThat(UserAgentErrorCode.USER_AGENT_NOT_FOUND.getHttpStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("INVALID_USER_AGENT_STATE - 코드는 USER-AGENT-002이고 상태는 400이다")
        void invalidUserAgentStateCode() {
            assertThat(UserAgentErrorCode.INVALID_USER_AGENT_STATE.getCode())
                    .isEqualTo("USER-AGENT-002");
            assertThat(UserAgentErrorCode.INVALID_USER_AGENT_STATE.getHttpStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT - 코드는 USER-AGENT-003이고 상태는 503이다")
        void noAvailableUserAgentCode() {
            assertThat(UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getCode())
                    .isEqualTo("USER-AGENT-003");
            assertThat(UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getHttpStatus()).isEqualTo(503);
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED - 코드는 USER-AGENT-004이고 상태는 429이다")
        void rateLimitExceededCode() {
            assertThat(UserAgentErrorCode.RATE_LIMIT_EXCEEDED.getCode())
                    .isEqualTo("USER-AGENT-004");
            assertThat(UserAgentErrorCode.RATE_LIMIT_EXCEEDED.getHttpStatus()).isEqualTo(429);
        }

        @Test
        @DisplayName("CIRCUIT_BREAKER_OPEN - 코드는 USER-AGENT-005이고 상태는 503이다")
        void circuitBreakerOpenCode() {
            assertThat(UserAgentErrorCode.CIRCUIT_BREAKER_OPEN.getCode())
                    .isEqualTo("USER-AGENT-005");
            assertThat(UserAgentErrorCode.CIRCUIT_BREAKER_OPEN.getHttpStatus()).isEqualTo(503);
        }

        @Test
        @DisplayName("USER_AGENT_BLOCKED - 코드는 USER-AGENT-006이고 상태는 403이다")
        void userAgentBlockedCode() {
            assertThat(UserAgentErrorCode.USER_AGENT_BLOCKED.getCode()).isEqualTo("USER-AGENT-006");
            assertThat(UserAgentErrorCode.USER_AGENT_BLOCKED.getHttpStatus()).isEqualTo(403);
        }

        @Test
        @DisplayName("INVALID_TOKEN - 코드는 USER-AGENT-007이고 상태는 400이다")
        void invalidTokenCode() {
            assertThat(UserAgentErrorCode.INVALID_TOKEN.getCode()).isEqualTo("USER-AGENT-007");
            assertThat(UserAgentErrorCode.INVALID_TOKEN.getHttpStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("SESSION_ISSUANCE_FAILED - 코드는 USER-AGENT-008이고 상태는 503이다")
        void sessionIssuanceFailedCode() {
            assertThat(UserAgentErrorCode.SESSION_ISSUANCE_FAILED.getCode())
                    .isEqualTo("USER-AGENT-008");
            assertThat(UserAgentErrorCode.SESSION_ISSUANCE_FAILED.getHttpStatus()).isEqualTo(503);
        }
    }
}
