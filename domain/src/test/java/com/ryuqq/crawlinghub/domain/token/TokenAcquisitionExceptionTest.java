package com.ryuqq.crawlinghub.domain.token;

import com.ryuqq.crawlinghub.domain.token.exception.TokenAcquisitionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenAcquisitionException 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("TokenAcquisitionException 단위 테스트")
class TokenAcquisitionExceptionTest {

    @Nested
    @DisplayName("예외 생성 테스트")
    class CreateExceptionTests {

        @Test
        @DisplayName("ErrorCode와 메시지로 예외 생성 성공")
        void shouldCreateExceptionWithErrorCodeAndMessage() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT;
            String message = "사용 가능한 User Agent가 없습니다";

            // When
            TokenAcquisitionException exception = new TokenAcquisitionException(errorCode, message);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(errorCode);
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.args()).isEmpty();
        }

        @Test
        @DisplayName("ErrorCode와 메시지, cause로 예외 생성 성공")
        void shouldCreateExceptionWithCause() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.TOKEN_ISSUANCE_FAILED;
            String message = "토큰 발급 중 오류 발생";
            Throwable cause = new RuntimeException("Network timeout");

            // When
            TokenAcquisitionException exception = new TokenAcquisitionException(errorCode, message, cause);

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(errorCode);
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("ErrorCode의 기본 메시지 확인")
        void shouldHaveDefaultMessage() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.RATE_LIMIT_EXCEEDED;

            // Then
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
            assertThat(errorCode.getDefaultMessage()).isEqualTo("Rate Limit을 초과했습니다");
        }
    }

    @Nested
    @DisplayName("Context 관리 테스트")
    class ContextManagementTests {

        @Test
        @DisplayName("withContext()로 컨텍스트 추가 성공 (Fluent Interface)")
        void shouldAddContextWithFluentInterface() {
            // Given
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT,
                "사용 가능한 User Agent가 없습니다"
            );

            // When
            TokenAcquisitionException result = exception
                .withContext("sellerId", 12345L)
                .withContext("attemptCount", 3);

            // Then
            assertThat(result).isSameAs(exception);  // Fluent Interface는 자기 자신 반환
            assertThat(exception.args()).hasSize(2);
            assertThat(exception.args()).containsEntry("sellerId", 12345L);
            assertThat(exception.args()).containsEntry("attemptCount", 3);
        }

        @Test
        @DisplayName("args()는 새로운 맵을 반환한다 (방어적 복사)")
        void shouldReturnNewMapFromArgs() {
            // Given
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.LOCK_ACQUISITION_FAILED,
                "락 획득 실패"
            ).withContext("lockKey", "token:12345");

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).containsEntry("lockKey", "token:12345");
        }

        @Test
        @DisplayName("여러 타입의 컨텍스트 추가 성공")
        void shouldAddMultipleTypesOfContext() {
            // Given
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.CIRCUIT_BREAKER_OPEN,
                "Circuit Breaker OPEN"
            );

            // When
            exception
                .withContext("sellerId", 12345L)
                .withContext("sellerName", "LIKEASTAR")
                .withContext("attemptCount", 3)
                .withContext("lastAttemptAt", "2025-11-07T15:00:00")
                .withContext("circuitBreakerState", "OPEN");

            // Then
            assertThat(exception.args()).hasSize(5);
            assertThat(exception.args().get("sellerId")).isInstanceOf(Long.class);
            assertThat(exception.args().get("sellerName")).isInstanceOf(String.class);
            assertThat(exception.args().get("attemptCount")).isInstanceOf(Integer.class);
        }
    }

    @Nested
    @DisplayName("ErrorCode Enum 테스트")
    class ErrorCodeEnumTests {

        @Test
        @DisplayName("ErrorCode는 5개의 유형을 가진다")
        void shouldHaveFiveErrorCodes() {
            // When
            TokenAcquisitionException.ErrorCode[] errorCodes = TokenAcquisitionException.ErrorCode.values();

            // Then
            assertThat(errorCodes).hasSize(5);
            assertThat(errorCodes).containsExactly(
                TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT,
                TokenAcquisitionException.ErrorCode.LOCK_ACQUISITION_FAILED,
                TokenAcquisitionException.ErrorCode.CIRCUIT_BREAKER_OPEN,
                TokenAcquisitionException.ErrorCode.TOKEN_ISSUANCE_FAILED,
                TokenAcquisitionException.ErrorCode.RATE_LIMIT_EXCEEDED
            );
        }

        @ParameterizedTest
        @EnumSource(TokenAcquisitionException.ErrorCode.class)
        @DisplayName("모든 ErrorCode는 code와 defaultMessage를 가진다")
        void shouldHaveCodeAndDefaultMessage(TokenAcquisitionException.ErrorCode errorCode) {
            // Then
            assertThat(errorCode.getCode()).isNotBlank();
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT 코드와 메시지 검증")
        void shouldHaveCorrectNoAvailableUserAgentCodeAndMessage() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT;

            // Then
            assertThat(errorCode.getCode()).isEqualTo("TOKEN-001");
            assertThat(errorCode.getDefaultMessage()).isEqualTo("사용 가능한 User Agent가 없습니다");
        }

        @Test
        @DisplayName("LOCK_ACQUISITION_FAILED 코드와 메시지 검증")
        void shouldHaveCorrectLockAcquisitionFailedCodeAndMessage() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.LOCK_ACQUISITION_FAILED;

            // Then
            assertThat(errorCode.getCode()).isEqualTo("TOKEN-002");
            assertThat(errorCode.getDefaultMessage()).isEqualTo("분산 락 획득에 실패했습니다");
        }

        @Test
        @DisplayName("CIRCUIT_BREAKER_OPEN 코드와 메시지 검증")
        void shouldHaveCorrectCircuitBreakerOpenCodeAndMessage() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.CIRCUIT_BREAKER_OPEN;

            // Then
            assertThat(errorCode.getCode()).isEqualTo("TOKEN-003");
            assertThat(errorCode.getDefaultMessage()).isEqualTo("Circuit Breaker가 OPEN 상태입니다");
        }

        @Test
        @DisplayName("TOKEN_ISSUANCE_FAILED 코드와 메시지 검증")
        void shouldHaveCorrectTokenIssuanceFailedCodeAndMessage() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.TOKEN_ISSUANCE_FAILED;

            // Then
            assertThat(errorCode.getCode()).isEqualTo("TOKEN-004");
            assertThat(errorCode.getDefaultMessage()).isEqualTo("토큰 발급에 실패했습니다");
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED 코드와 메시지 검증")
        void shouldHaveCorrectRateLimitExceededCodeAndMessage() {
            // Given
            TokenAcquisitionException.ErrorCode errorCode = TokenAcquisitionException.ErrorCode.RATE_LIMIT_EXCEEDED;

            // Then
            assertThat(errorCode.getCode()).isEqualTo("TOKEN-005");
            assertThat(errorCode.getDefaultMessage()).isEqualTo("Rate Limit을 초과했습니다");
        }
    }

    @Nested
    @DisplayName("예외 메시지 조합 테스트")
    class ExceptionMessageCompositionTests {

        @Test
        @DisplayName("컨텍스트 정보를 포함한 상세 예외 메시지 구성")
        void shouldComposeDetailedExceptionMessage() {
            // Given
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT,
                "사용 가능한 User Agent가 없습니다"
            ).withContext("sellerId", 12345L)
             .withContext("attemptCount", 3);

            // Then
            assertThat(exception.getMessage()).isEqualTo("사용 가능한 User Agent가 없습니다");
            assertThat(exception.getErrorCode().getCode()).isEqualTo("TOKEN-001");
            assertThat(exception.args()).containsEntry("sellerId", 12345L);
            assertThat(exception.args()).containsEntry("attemptCount", 3);
        }

        @Test
        @DisplayName("cause를 포함한 예외 체인 구성")
        void shouldComposeExceptionChainWithCause() {
            // Given
            RuntimeException rootCause = new RuntimeException("Network error");
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.TOKEN_ISSUANCE_FAILED,
                "토큰 발급 중 네트워크 오류 발생",
                rootCause
            ).withContext("endpoint", "https://api.mustit.co.kr/token");

            // Then
            assertThat(exception.getMessage()).contains("토큰 발급 중 네트워크 오류 발생");
            assertThat(exception.getCause()).isEqualTo(rootCause);
            assertThat(exception.getCause().getMessage()).isEqualTo("Network error");
        }
    }

    @Nested
    @DisplayName("실전 시나리오 테스트")
    class RealWorldScenarioTests {

        @Test
        @DisplayName("User Agent 풀 고갈 시나리오")
        void shouldHandleNoAvailableUserAgentScenario() {
            // Given
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT,
                "모든 User Agent가 사용 중입니다"
            ).withContext("totalAgents", 10)
             .withContext("inUseAgents", 10)
             .withContext("waitingRequests", 5);

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT);
            assertThat(exception.args().get("totalAgents")).isEqualTo(10);
            assertThat(exception.args().get("inUseAgents")).isEqualTo(10);
        }

        @Test
        @DisplayName("분산 락 획득 실패 시나리오")
        void shouldHandleLockAcquisitionFailedScenario() {
            // Given
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.LOCK_ACQUISITION_FAILED,
                "락 대기 시간 초과"
            ).withContext("lockKey", "token:seller:12345")
             .withContext("waitTimeMs", 5000L)
             .withContext("lockOwner", "worker-thread-42");

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(TokenAcquisitionException.ErrorCode.LOCK_ACQUISITION_FAILED);
            assertThat(exception.args().get("lockKey")).isEqualTo("token:seller:12345");
            assertThat(exception.args().get("waitTimeMs")).isEqualTo(5000L);
        }

        @Test
        @DisplayName("Circuit Breaker OPEN 시나리오")
        void shouldHandleCircuitBreakerOpenScenario() {
            // Given
            TokenAcquisitionException exception = new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.CIRCUIT_BREAKER_OPEN,
                "토큰 발급 API Circuit Breaker OPEN"
            ).withContext("failureCount", 10)
             .withContext("threshold", 5)
             .withContext("resetTimeout", 60000L);

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(TokenAcquisitionException.ErrorCode.CIRCUIT_BREAKER_OPEN);
            assertThat(exception.args().get("failureCount")).isEqualTo(10);
        }
    }
}
