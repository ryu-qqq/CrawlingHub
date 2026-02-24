package com.ryuqq.crawlinghub.adapter.in.rest.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * GlobalExceptionHandler 추가 단위 테스트
 *
 * <p>기존 테스트에서 커버되지 않은 케이스들을 검증합니다.
 *
 * <ul>
 *   <li>AuthenticationException 처리 (401)
 *   <li>AccessDeniedException 처리 (403)
 *   <li>5xx DomainException 처리
 *   <li>MDC traceId/spanId 포함 여부
 *   <li>쿼리스트링 공백 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("error")
@DisplayName("GlobalExceptionHandler 추가 단위 테스트")
class GlobalExceptionHandlerAdditionalTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private ErrorMapperRegistry errorMapperRegistry;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        errorMapperRegistry = Mockito.mock(ErrorMapperRegistry.class);
        globalExceptionHandler = new GlobalExceptionHandler(errorMapperRegistry);

        mockRequest = Mockito.mock(HttpServletRequest.class);
        given(mockRequest.getRequestURI()).willReturn("/api/test");
        given(mockRequest.getQueryString()).willReturn(null);

        MDC.clear();
    }

    @Nested
    @DisplayName("handleAuthenticationException()은")
    class HandleAuthenticationExceptionTest {

        @Test
        @DisplayName("AuthenticationException을 401 UNAUTHORIZED로 변환한다")
        void shouldReturn401ForAuthenticationException() {
            // Given
            BadCredentialsException exception = new BadCredentialsException("인증 정보가 올바르지 않습니다");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleAuthenticationException(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Unauthorized");
            assertThat(response.getBody().getDetail()).isEqualTo("인증이 필요합니다.");
        }

        @Test
        @DisplayName("401 응답의 code는 UNAUTHORIZED이다")
        void shouldHaveUnauthorizedCode() {
            // Given
            BadCredentialsException exception = new BadCredentialsException("인증 실패");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleAuthenticationException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getProperties()).containsEntry("code", "UNAUTHORIZED");
        }
    }

    @Nested
    @DisplayName("handleAccessDeniedException()은")
    class HandleAccessDeniedExceptionTest {

        @Test
        @DisplayName("AccessDeniedException을 403 FORBIDDEN으로 변환한다")
        void shouldReturn403ForAccessDeniedException() {
            // Given
            AccessDeniedException exception = new AccessDeniedException("접근 권한이 없습니다");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleAccessDeniedException(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Forbidden");
            assertThat(response.getBody().getDetail()).isEqualTo("접근 권한이 없습니다.");
        }

        @Test
        @DisplayName("403 응답의 code는 FORBIDDEN이다")
        void shouldHaveForbiddenCode() {
            // Given
            AccessDeniedException exception = new AccessDeniedException("권한 없음");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleAccessDeniedException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getProperties()).containsEntry("code", "FORBIDDEN");
        }
    }

    @Nested
    @DisplayName("MDC 컨텍스트 포함 검증")
    class MdcContextTest {

        @Test
        @DisplayName("MDC에 traceId가 있으면 응답에 traceId가 포함된다")
        void shouldIncludeTraceIdFromMdc() {
            // Given
            String traceId = "abc123";
            MDC.put("traceId", traceId);
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            try {
                // When
                ResponseEntity<ProblemDetail> response =
                        globalExceptionHandler.handleIllegalArgumentException(
                                exception, mockRequest);

                // Then
                assertThat(response.getBody().getProperties()).containsEntry("traceId", traceId);
            } finally {
                MDC.clear();
            }
        }

        @Test
        @DisplayName("MDC에 spanId가 있으면 응답에 spanId가 포함된다")
        void shouldIncludeSpanIdFromMdc() {
            // Given
            String spanId = "span456";
            MDC.put("spanId", spanId);
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            try {
                // When
                ResponseEntity<ProblemDetail> response =
                        globalExceptionHandler.handleIllegalArgumentException(
                                exception, mockRequest);

                // Then
                assertThat(response.getBody().getProperties()).containsEntry("spanId", spanId);
            } finally {
                MDC.clear();
            }
        }

        @Test
        @DisplayName("MDC에 traceId가 없으면 응답에 traceId가 포함되지 않는다")
        void shouldNotIncludeTraceIdWhenMdcIsEmpty() {
            // Given
            MDC.remove("traceId");
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getProperties()).doesNotContainKey("traceId");
        }
    }

    @Nested
    @DisplayName("handleDomain() - 5xx 서버 에러 처리")
    class HandleDomain5xxTest {

        @Test
        @DisplayName("5xx DomainException은 500 INTERNAL_SERVER_ERROR로 처리된다")
        void shouldReturn500ForServerErrorDomainException() {
            // Given
            DomainException exception =
                    createDomainExceptionWithHttpStatus("SERVER-ERROR", "서버 에러", 500);
            MappedError mappedError =
                    new MappedError(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Internal Server Error",
                            "서버 오류가 발생했습니다",
                            URI.create("about:blank"));

            given(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .willReturn(Optional.of(mappedError));

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleDomain(exception, mockRequest, Locale.KOREA);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("404 DomainException은 NOT_FOUND로 처리된다")
        void shouldReturn404ForNotFoundDomainException() {
            // Given
            DomainException exception =
                    createDomainExceptionWithHttpStatus("NOT-FOUND", "찾을 수 없음", 404);
            MappedError mappedError =
                    new MappedError(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            "리소스를 찾을 수 없습니다",
                            URI.create("about:blank"));

            given(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .willReturn(Optional.of(mappedError));

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleDomain(exception, mockRequest, Locale.KOREA);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("409 DomainException은 CONFLICT로 처리된다")
        void shouldReturn409ForConflictDomainException() {
            // Given
            DomainException exception = createDomainExceptionWithHttpStatus("CONFLICT", "충돌", 409);
            MappedError mappedError =
                    new MappedError(
                            HttpStatus.CONFLICT,
                            "Conflict",
                            "상태 충돌이 발생했습니다",
                            URI.create("about:blank"));

            given(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .willReturn(Optional.of(mappedError));

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleDomain(exception, mockRequest, Locale.KOREA);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    @DisplayName("쿼리스트링 처리 검증")
    class QueryStringHandlingTest {

        @Test
        @DisplayName("쿼리스트링이 공백이면 instance에 포함되지 않는다")
        void shouldNotIncludeBlankQueryStringInInstance() {
            // Given
            given(mockRequest.getRequestURI()).willReturn("/api/test");
            given(mockRequest.getQueryString()).willReturn("   ");
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            // isBlank() 조건: 공백만 있는 쿼리스트링은 URI에 포함되지 않아야 함
            assertThat(response.getBody().getInstance()).isEqualTo(URI.create("/api/test"));
        }

        @Test
        @DisplayName("쿼리스트링이 null이면 instance에 포함되지 않는다")
        void shouldNotIncludeNullQueryStringInInstance() {
            // Given
            given(mockRequest.getRequestURI()).willReturn("/api/test");
            given(mockRequest.getQueryString()).willReturn(null);
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getInstance()).isEqualTo(URI.create("/api/test"));
        }

        @Test
        @DisplayName("쿼리스트링이 있으면 instance에 포함된다")
        void shouldIncludeQueryStringInInstance() {
            // Given
            given(mockRequest.getRequestURI()).willReturn("/api/test");
            given(mockRequest.getQueryString()).willReturn("page=0&size=10");
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getInstance())
                    .isEqualTo(URI.create("/api/test?page=0&size=10"));
        }
    }

    @Nested
    @DisplayName("ErrorMapperRegistry defaultMapping 검증")
    class DefaultMappingHttpStatusTest {

        @Test
        @DisplayName("유효하지 않은 httpStatus 코드면 BAD_REQUEST로 대체된다")
        void shouldUseBadRequestWhenHttpStatusIsInvalid() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(Collections.emptyList());
            DomainException exception =
                    new DomainException(createMockErrorCode("INVALID", "에러"), "에러 메시지") {
                        @Override
                        public String code() {
                            return "INVALID";
                        }

                        @Override
                        public int httpStatus() {
                            return 999; // 유효하지 않은 HTTP 상태 코드
                        }
                    };

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("httpStatus가 404이면 title이 Not Found이다")
        void shouldHaveNotFoundTitleWhen404() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(Collections.emptyList());
            DomainException exception =
                    new DomainException(createMockErrorCode("NOT_FOUND", "없음"), "없음") {
                        @Override
                        public String code() {
                            return "NOT_FOUND";
                        }

                        @Override
                        public int httpStatus() {
                            return 404;
                        }
                    };

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.title()).isEqualTo("Not Found");
        }

        @Test
        @DisplayName("httpStatus가 409이면 title이 Conflict이다")
        void shouldHaveConflictTitleWhen409() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(Collections.emptyList());
            DomainException exception =
                    new DomainException(createMockErrorCode("CONFLICT", "충돌"), "충돌") {
                        @Override
                        public String code() {
                            return "CONFLICT";
                        }

                        @Override
                        public int httpStatus() {
                            return 409;
                        }
                    };

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.title()).isEqualTo("Conflict");
        }

        @Test
        @DisplayName("httpStatus가 5xx이면 title이 Server Error이다")
        void shouldHaveServerErrorTitleWhen5xx() {
            // Given
            ErrorMapperRegistry registry = new ErrorMapperRegistry(Collections.emptyList());
            DomainException exception =
                    new DomainException(createMockErrorCode("SERVER_ERROR", "서버 에러"), "서버 에러") {
                        @Override
                        public String code() {
                            return "SERVER_ERROR";
                        }

                        @Override
                        public int httpStatus() {
                            return 500;
                        }
                    };

            // When
            MappedError result = registry.defaultMapping(exception);

            // Then
            assertThat(result.title()).isEqualTo("Server Error");
        }
    }

    private DomainException createDomainExceptionWithHttpStatus(
            String code, String message, int httpStatusCode) {
        ErrorCode errorCode = createMockErrorCode(code, message);

        return new DomainException(errorCode, message) {
            @Override
            public String code() {
                return code;
            }

            @Override
            public int httpStatus() {
                return httpStatusCode;
            }

            @Override
            public Map<String, Object> args() {
                return Collections.emptyMap();
            }
        };
    }

    private ErrorCode createMockErrorCode(String code, String message) {
        ErrorCode errorCode = Mockito.mock(ErrorCode.class);
        given(errorCode.getCode()).willReturn(code);
        given(errorCode.getMessage()).willReturn(message);
        return errorCode;
    }
}
