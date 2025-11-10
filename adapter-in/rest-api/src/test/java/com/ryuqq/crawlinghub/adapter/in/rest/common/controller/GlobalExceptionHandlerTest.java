package com.ryuqq.crawlinghub.adapter.in.rest.common.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * GlobalExceptionHandler 단위 테스트
 *
 * <p>REST API Layer의 예외 처리 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorMapperRegistry errorMapperRegistry;

    @Mock
    private HttpServletRequest httpServletRequest;

    private GlobalExceptionHandler sut;

    @BeforeEach
    void setUp() {
        sut = new GlobalExceptionHandler(errorMapperRegistry);

        // HttpServletRequest 기본 Mock 설정
        given(httpServletRequest.getRequestURI()).willReturn("/api/test");
        given(httpServletRequest.getQueryString()).willReturn(null);
    }

    @Nested
    @DisplayName("handleValidationException 메서드는")
    class Describe_handleValidationException {

        @Nested
        @DisplayName("MethodArgumentNotValidException이 발생하면")
        class Context_with_method_argument_not_valid_exception {

            private MethodArgumentNotValidException exception;

            @BeforeEach
            void setUp() {
                // BindException을 상속한 MethodArgumentNotValidException Mock
                BindException bindException = new BindException(new Object(), "target");
                bindException.addError(new FieldError("target", "name", "이름은 필수입니다"));
                bindException.addError(new FieldError("target", "age", "나이는 0 이상이어야 합니다"));

                exception = new MethodArgumentNotValidException(null, bindException);
            }

            @Test
            @DisplayName("400 Bad Request와 errors 맵을 반환한다")
            void it_returns_bad_request_with_errors() {
                // When
                ResponseEntity<ProblemDetail> response = sut.handleValidationException(exception, httpServletRequest);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getStatus()).isEqualTo(400);
                assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
                assertThat(response.getBody().getDetail()).isEqualTo("Validation failed for request");

                @SuppressWarnings("unchecked")
                Map<String, String> errors = (Map<String, String>) response.getBody().getProperties().get("errors");
                assertThat(errors).isNotNull();
                assertThat(errors).containsEntry("name", "이름은 필수입니다");
                assertThat(errors).containsEntry("age", "나이는 0 이상이어야 합니다");
            }
        }
    }

    @Nested
    @DisplayName("handleConstraintViolation 메서드는")
    class Describe_handleConstraintViolation {

        @Nested
        @DisplayName("ConstraintViolationException이 발생하면")
        class Context_with_constraint_violation_exception {

            private ConstraintViolationException exception;

            @BeforeEach
            void setUp() {
                @SuppressWarnings("unchecked")
                ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
                given(violation.getPropertyPath()).willReturn(mock(jakarta.validation.Path.class));
                given(violation.getPropertyPath().toString()).willReturn("userId");
                given(violation.getMessage()).willReturn("양수여야 합니다");

                exception = new ConstraintViolationException(Set.of(violation));
            }

            @Test
            @DisplayName("400 Bad Request와 errors 맵을 반환한다")
            void it_returns_bad_request_with_errors() {
                // When
                ResponseEntity<ProblemDetail> response = sut.handleConstraintViolation(exception, httpServletRequest);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody()).isNotNull();

                @SuppressWarnings("unchecked")
                Map<String, String> errors = (Map<String, String>) response.getBody().getProperties().get("errors");
                assertThat(errors).containsKey("userId");
                assertThat(errors.get("userId")).isEqualTo("양수여야 합니다");
            }
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException 메서드는")
    class Describe_handleIllegalArgumentException {

        @Test
        @DisplayName("IllegalArgumentException이 발생하면 400 Bad Request를 반환한다")
        void it_returns_bad_request() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException("잘못된 인자입니다");

            // When
            ResponseEntity<ProblemDetail> response = sut.handleIllegalArgumentException(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).isEqualTo("잘못된 인자입니다");
        }
    }

    @Nested
    @DisplayName("handleHttpMessageNotReadable 메서드는")
    class Describe_handleHttpMessageNotReadable {

        @Test
        @DisplayName("HttpMessageNotReadableException이 발생하면 400 Bad Request를 반환한다")
        void it_returns_bad_request() {
            // Given
            HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
            Throwable cause = new RuntimeException("JSON parse error");
            given(exception.getMostSpecificCause()).willReturn(cause);

            // When
            ResponseEntity<ProblemDetail> response = sut.handleHttpMessageNotReadable(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("JSON 형식을 확인해주세요");
        }
    }

    @Nested
    @DisplayName("handleTypeMismatch 메서드는")
    class Describe_handleTypeMismatch {

        @Test
        @DisplayName("MethodArgumentTypeMismatchException이 발생하면 400 Bad Request를 반환한다")
        void it_returns_bad_request() {
            // Given
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            given(exception.getName()).willReturn("userId");
            given(exception.getValue()).willReturn("invalid");
            given(exception.getRequiredType()).willReturn(null); // null 가능

            // When
            ResponseEntity<ProblemDetail> response = sut.handleTypeMismatch(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("userId");
        }
    }

    @Nested
    @DisplayName("handleMissingParam 메서드는")
    class Describe_handleMissingParam {

        @Test
        @DisplayName("MissingServletRequestParameterException이 발생하면 400 Bad Request를 반환한다")
        void it_returns_bad_request() {
            // Given
            MissingServletRequestParameterException exception = new MissingServletRequestParameterException("userId", "Long");

            // When
            ResponseEntity<ProblemDetail> response = sut.handleMissingParam(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("userId");
            assertThat(response.getBody().getDetail()).contains("필수 파라미터");
        }
    }

    @Nested
    @DisplayName("handleNoResource 메서드는")
    class Describe_handleNoResource {

        @Test
        @DisplayName("NoResourceFoundException이 발생하면 404 Not Found를 반환한다")
        void it_returns_not_found() {
            // Given
            NoResourceFoundException exception = mock(NoResourceFoundException.class);
            given(exception.getResourcePath()).willReturn("/api/unknown");

            // When
            ResponseEntity<ProblemDetail> response = sut.handleNoResource(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("리소스를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("handleMethodNotAllowed 메서드는")
    class Describe_handleMethodNotAllowed {

        @Test
        @DisplayName("HttpRequestMethodNotSupportedException이 발생하면 405 Method Not Allowed를 반환한다")
        void it_returns_method_not_allowed() {
            // Given
            HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException(
                "DELETE",
                Set.of("GET", "POST")
            );

            // When
            ResponseEntity<ProblemDetail> response = sut.handleMethodNotAllowed(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("DELETE");
            assertThat(response.getHeaders().getAllow()).contains(HttpMethod.GET, HttpMethod.POST);
        }
    }

    @Nested
    @DisplayName("handleIllegalState 메서드는")
    class Describe_handleIllegalState {

        @Test
        @DisplayName("IllegalStateException이 발생하면 409 Conflict를 반환한다")
        void it_returns_conflict() {
            // Given
            IllegalStateException exception = new IllegalStateException("이미 처리된 요청입니다");

            // When
            ResponseEntity<ProblemDetail> response = sut.handleIllegalState(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("이미 처리된 요청입니다");
        }
    }

    @Nested
    @DisplayName("handleGlobal 메서드는")
    class Describe_handleGlobal {

        @Test
        @DisplayName("예상하지 못한 예외가 발생하면 500 Internal Server Error를 반환한다")
        void it_returns_internal_server_error() {
            // Given
            Exception exception = new RuntimeException("예상하지 못한 에러");

            // When
            ResponseEntity<ProblemDetail> response = sut.handleGlobal(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("서버 오류가 발생했습니다");
        }
    }
}
