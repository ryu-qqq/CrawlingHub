package com.ryuqq.crawlinghub.domain.useragent.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserAgentErrorCode 테스트")
class UserAgentErrorCodeTest {

    @Nested
    @DisplayName("Enum 값 테스트")
    class EnumValueTests {

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT 상수 존재")
        void shouldHaveNoAvailableUserAgent() {
            // Given & When
            UserAgentErrorCode errorCode = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("NO_AVAILABLE_USER_AGENT");
        }

        @Test
        @DisplayName("INVALID_USER_AGENT 상수 존재")
        void shouldHaveInvalidUserAgent() {
            // Given & When
            UserAgentErrorCode errorCode = UserAgentErrorCode.INVALID_USER_AGENT;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("INVALID_USER_AGENT");
        }

        @Test
        @DisplayName("TOKEN_EXPIRED 상수 존재")
        void shouldHaveTokenExpired() {
            // Given & When
            UserAgentErrorCode errorCode = UserAgentErrorCode.TOKEN_EXPIRED;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("TOKEN_EXPIRED");
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED 상수 존재")
        void shouldHaveRateLimitExceeded() {
            // Given & When
            UserAgentErrorCode errorCode = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("RATE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("Enum 값은 정확히 4개")
        void shouldHaveExactlyFourValues() {
            // When
            UserAgentErrorCode[] values = UserAgentErrorCode.values();

            // Then
            assertThat(values).hasSize(4);
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 Enum 값이 유효")
        void shouldHaveValidValues(UserAgentErrorCode errorCode) {
            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("getCode() 메서드 테스트")
    class GetCodeTests {

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT는 'USER_AGENT-001' 반환")
        void shouldReturnNoAvailableUserAgentCode() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("USER_AGENT-001");
        }

        @Test
        @DisplayName("INVALID_USER_AGENT는 'USER_AGENT-101' 반환")
        void shouldReturnInvalidUserAgentCode() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.INVALID_USER_AGENT;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("USER_AGENT-101");
        }

        @Test
        @DisplayName("TOKEN_EXPIRED는 'USER_AGENT-102' 반환")
        void shouldReturnTokenExpiredCode() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.TOKEN_EXPIRED;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("USER_AGENT-102");
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED는 'USER_AGENT-201' 반환")
        void shouldReturnRateLimitExceededCode() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("USER_AGENT-201");
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 Error Code는 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyCode(UserAgentErrorCode errorCode) {
            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 Error Code는 'USER_AGENT-' 접두사로 시작")
        void shouldStartWithUserAgentPrefix(UserAgentErrorCode errorCode) {
            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).startsWith("USER_AGENT-");
        }

        @Test
        @DisplayName("Error Code는 모두 고유함 (중복 없음)")
        void shouldHaveUniqueCodes() {
            // When
            long distinctCount = java.util.Arrays.stream(UserAgentErrorCode.values())
                .map(UserAgentErrorCode::getCode)
                .distinct()
                .count();

            // Then
            assertThat(distinctCount).isEqualTo(4);  // 4개 모두 고유
        }
    }

    @Nested
    @DisplayName("getHttpStatus() 메서드 테스트")
    class GetHttpStatusTests {

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT는 404 반환")
        void shouldReturn404ForNoAvailableUserAgent() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(404);
        }

        @Test
        @DisplayName("INVALID_USER_AGENT는 400 반환")
        void shouldReturn400ForInvalidUserAgent() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.INVALID_USER_AGENT;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(400);
        }

        @Test
        @DisplayName("TOKEN_EXPIRED는 400 반환")
        void shouldReturn400ForTokenExpired() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.TOKEN_EXPIRED;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(400);
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED는 429 반환")
        void shouldReturn429ForRateLimitExceeded() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(429);
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 HTTP 상태 코드는 유효한 범위 (400-599)")
        void shouldHaveValidHttpStatus(UserAgentErrorCode errorCode) {
            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isBetween(400, 599);
        }

        @Test
        @DisplayName("HTTP 상태 코드는 404, 400, 429만 사용")
        void shouldUseSpecificHttpStatusCodes() {
            // When
            long distinctStatusCount = java.util.Arrays.stream(UserAgentErrorCode.values())
                .map(UserAgentErrorCode::getHttpStatus)
                .distinct()
                .count();

            // Then
            assertThat(distinctStatusCount).isEqualTo(3);  // 404, 400, 429 (3가지)
        }
    }

    @Nested
    @DisplayName("getMessage() 메서드 테스트")
    class GetMessageTests {

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT 메시지 확인")
        void shouldReturnNoAvailableUserAgentMessage() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("사용 가능한 User-Agent를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("INVALID_USER_AGENT 메시지 확인")
        void shouldReturnInvalidUserAgentMessage() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.INVALID_USER_AGENT;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("유효하지 않은 User-Agent 문자열입니다");
        }

        @Test
        @DisplayName("TOKEN_EXPIRED 메시지 확인")
        void shouldReturnTokenExpiredMessage() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.TOKEN_EXPIRED;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("토큰이 만료되었습니다");
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED 메시지 확인")
        void shouldReturnRateLimitExceededMessage() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("Rate Limit을 초과했습니다");
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 메시지는 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyMessage(UserAgentErrorCode errorCode) {
            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("모든 메시지는 고유함 (중복 없음)")
        void shouldHaveUniqueMessages() {
            // When
            long distinctCount = java.util.Arrays.stream(UserAgentErrorCode.values())
                .map(UserAgentErrorCode::getMessage)
                .distinct()
                .count();

            // Then
            assertThat(distinctCount).isEqualTo(4);  // 4개 모두 고유
        }
    }

    @Nested
    @DisplayName("getTitle() 메서드 테스트")
    class GetTitleTests {

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT 타이틀 확인")
        void shouldReturnNoAvailableUserAgentTitle() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("No Available User Agent");
        }

        @Test
        @DisplayName("INVALID_USER_AGENT 타이틀 확인")
        void shouldReturnInvalidUserAgentTitle() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.INVALID_USER_AGENT;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("Invalid User Agent");
        }

        @Test
        @DisplayName("TOKEN_EXPIRED 타이틀 확인")
        void shouldReturnTokenExpiredTitle() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.TOKEN_EXPIRED;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("Token Expired");
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED 타이틀 확인")
        void shouldReturnRateLimitExceededTitle() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("Rate Limit Exceeded");
        }

        @ParameterizedTest
        @EnumSource(UserAgentErrorCode.class)
        @DisplayName("모든 타이틀은 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyTitle(UserAgentErrorCode errorCode) {
            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("모든 타이틀은 고유함 (중복 없음)")
        void shouldHaveUniqueTitles() {
            // When
            long distinctCount = java.util.Arrays.stream(UserAgentErrorCode.values())
                .map(UserAgentErrorCode::getTitle)
                .distinct()
                .count();

            // Then
            assertThat(distinctCount).isEqualTo(4);  // 4개 모두 고유
        }
    }

    @Nested
    @DisplayName("valueOf() 메서드 테스트")
    class ValueOfTests {

        @Test
        @DisplayName("문자열 'NO_AVAILABLE_USER_AGENT'로 Enum 생성")
        void shouldCreateFromNoAvailableUserAgentString() {
            // When
            UserAgentErrorCode errorCode = UserAgentErrorCode.valueOf("NO_AVAILABLE_USER_AGENT");

            // Then
            assertThat(errorCode).isEqualTo(UserAgentErrorCode.NO_AVAILABLE_USER_AGENT);
        }

        @Test
        @DisplayName("문자열 'INVALID_USER_AGENT'로 Enum 생성")
        void shouldCreateFromInvalidUserAgentString() {
            // When
            UserAgentErrorCode errorCode = UserAgentErrorCode.valueOf("INVALID_USER_AGENT");

            // Then
            assertThat(errorCode).isEqualTo(UserAgentErrorCode.INVALID_USER_AGENT);
        }

        @Test
        @DisplayName("문자열 'TOKEN_EXPIRED'로 Enum 생성")
        void shouldCreateFromTokenExpiredString() {
            // When
            UserAgentErrorCode errorCode = UserAgentErrorCode.valueOf("TOKEN_EXPIRED");

            // Then
            assertThat(errorCode).isEqualTo(UserAgentErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("문자열 'RATE_LIMIT_EXCEEDED'로 Enum 생성")
        void shouldCreateFromRateLimitExceededString() {
            // When
            UserAgentErrorCode errorCode = UserAgentErrorCode.valueOf("RATE_LIMIT_EXCEEDED");

            // Then
            assertThat(errorCode).isEqualTo(UserAgentErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("Enum 비교 테스트")
    class ComparisonTests {

        @Test
        @DisplayName("같은 Enum 값은 동일하다")
        void shouldBeEqualForSameEnum() {
            // Given
            UserAgentErrorCode errorCode1 = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;
            UserAgentErrorCode errorCode2 = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // Then
            assertThat(errorCode1).isEqualTo(errorCode2);
            assertThat(errorCode1).isSameAs(errorCode2);  // Enum은 싱글톤
        }

        @Test
        @DisplayName("다른 Enum 값은 동일하지 않다")
        void shouldNotBeEqualForDifferentEnum() {
            // Given
            UserAgentErrorCode errorCode1 = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;
            UserAgentErrorCode errorCode2 = UserAgentErrorCode.INVALID_USER_AGENT;

            // Then
            assertThat(errorCode1).isNotEqualTo(errorCode2);
        }

        @Test
        @DisplayName("Enum 순서 확인")
        void shouldHaveCorrectOrdinal() {
            // Given
            UserAgentErrorCode[] values = UserAgentErrorCode.values();

            // Then
            assertThat(values[0]).isEqualTo(UserAgentErrorCode.NO_AVAILABLE_USER_AGENT);
            assertThat(values[1]).isEqualTo(UserAgentErrorCode.INVALID_USER_AGENT);
            assertThat(values[2]).isEqualTo(UserAgentErrorCode.TOKEN_EXPIRED);
            assertThat(values[3]).isEqualTo(UserAgentErrorCode.RATE_LIMIT_EXCEEDED);

            assertThat(UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.ordinal()).isEqualTo(0);
            assertThat(UserAgentErrorCode.INVALID_USER_AGENT.ordinal()).isEqualTo(1);
            assertThat(UserAgentErrorCode.TOKEN_EXPIRED.ordinal()).isEqualTo(2);
            assertThat(UserAgentErrorCode.RATE_LIMIT_EXCEEDED.ordinal()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("NO_AVAILABLE_USER_AGENT toString 확인")
        void shouldReturnNoAvailableUserAgentAsString() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("NO_AVAILABLE_USER_AGENT");
        }

        @Test
        @DisplayName("INVALID_USER_AGENT toString 확인")
        void shouldReturnInvalidUserAgentAsString() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.INVALID_USER_AGENT;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("INVALID_USER_AGENT");
        }

        @Test
        @DisplayName("TOKEN_EXPIRED toString 확인")
        void shouldReturnTokenExpiredAsString() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.TOKEN_EXPIRED;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("TOKEN_EXPIRED");
        }

        @Test
        @DisplayName("RATE_LIMIT_EXCEEDED toString 확인")
        void shouldReturnRateLimitExceededAsString() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("RATE_LIMIT_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("404 Not Found 에러 코드 사용 시나리오")
        void shouldHandleNotFoundScenario() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("USER_AGENT-001");
            assertThat(errorCode.getHttpStatus()).isEqualTo(404);
            assertThat(errorCode.getMessage()).isEqualTo("사용 가능한 User-Agent를 찾을 수 없습니다");
            assertThat(errorCode.getTitle()).isEqualTo("No Available User Agent");
        }

        @Test
        @DisplayName("400 Bad Request 에러 코드 사용 시나리오")
        void shouldHandleBadRequestScenario() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.INVALID_USER_AGENT;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("USER_AGENT-101");
            assertThat(errorCode.getHttpStatus()).isEqualTo(400);
            assertThat(errorCode.getMessage()).isEqualTo("유효하지 않은 User-Agent 문자열입니다");
            assertThat(errorCode.getTitle()).isEqualTo("Invalid User Agent");
        }

        @Test
        @DisplayName("429 Too Many Requests 에러 코드 사용 시나리오")
        void shouldHandleTooManyRequestsScenario() {
            // Given
            UserAgentErrorCode errorCode = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("USER_AGENT-201");
            assertThat(errorCode.getHttpStatus()).isEqualTo(429);
            assertThat(errorCode.getMessage()).isEqualTo("Rate Limit을 초과했습니다");
            assertThat(errorCode.getTitle()).isEqualTo("Rate Limit Exceeded");
        }

        @Test
        @DisplayName("Switch 문에서 모든 에러 코드 처리 가능")
        void shouldHandleAllErrorCodesInSwitch() {
            // Given
            UserAgentErrorCode[] allErrorCodes = UserAgentErrorCode.values();

            // When & Then
            for (UserAgentErrorCode errorCode : allErrorCodes) {
                String result = switch (errorCode) {
                    case NO_AVAILABLE_USER_AGENT -> "No Available";
                    case INVALID_USER_AGENT -> "Invalid";
                    case TOKEN_EXPIRED -> "Expired";
                    case RATE_LIMIT_EXCEEDED -> "Rate Limit";
                };
                assertThat(result).isNotBlank();
            }
        }

        @Test
        @DisplayName("HTTP 상태 코드별 그룹화 가능")
        void shouldGroupByHttpStatus() {
            // Given
            UserAgentErrorCode[] allErrorCodes = UserAgentErrorCode.values();

            // When
            long notFoundCount = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getHttpStatus() == 404)
                .count();
            long badRequestCount = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getHttpStatus() == 400)
                .count();
            long tooManyRequestsCount = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getHttpStatus() == 429)
                .count();

            // Then
            assertThat(notFoundCount).isEqualTo(1);  // NO_AVAILABLE_USER_AGENT
            assertThat(badRequestCount).isEqualTo(2);  // INVALID_USER_AGENT, TOKEN_EXPIRED
            assertThat(tooManyRequestsCount).isEqualTo(1);  // RATE_LIMIT_EXCEEDED
        }

        @Test
        @DisplayName("Error Code 범위별 그룹화 가능")
        void shouldGroupByCodeRange() {
            // Given
            UserAgentErrorCode[] allErrorCodes = UserAgentErrorCode.values();

            // When
            long notFoundRange = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getCode().compareTo("USER_AGENT-001") >= 0
                             && code.getCode().compareTo("USER_AGENT-009") <= 0)
                .count();
            long badRequestRange = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getCode().compareTo("USER_AGENT-101") >= 0
                             && code.getCode().compareTo("USER_AGENT-199") <= 0)
                .count();
            long rateLimitRange = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getCode().compareTo("USER_AGENT-201") >= 0
                             && code.getCode().compareTo("USER_AGENT-299") <= 0)
                .count();

            // Then
            assertThat(notFoundRange).isEqualTo(1);  // USER_AGENT-001
            assertThat(badRequestRange).isEqualTo(2);  // USER_AGENT-101, USER_AGENT-102
            assertThat(rateLimitRange).isEqualTo(1);  // USER_AGENT-201
        }
    }
}
