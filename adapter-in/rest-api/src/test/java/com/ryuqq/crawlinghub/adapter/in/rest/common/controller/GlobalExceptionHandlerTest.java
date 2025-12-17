package com.ryuqq.crawlinghub.adapter.in.rest.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * GlobalExceptionHandler 단위 테스트
 *
 * <p>전역 예외 처리기의 RFC 7807 ProblemDetail 응답 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>각 예외별 HttpStatus 매핑 검증
 *   <li>ProblemDetail 응답 구조 검증
 *   <li>에러 정보 포함 검증 (errors, timestamp, instance 등)
 *   <li>DomainException 처리 및 ErrorMapperRegistry 연동 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("error")
@DisplayName("GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest {

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
    }

    @Nested
    @DisplayName("handleValidationException()은")
    class HandleValidationException {

        @Test
        @DisplayName("MethodArgumentNotValidException을 400 BAD_REQUEST로 변환한다")
        void shouldReturn400ForMethodArgumentNotValidException() {
            // Given
            BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "testObject");
            bindingResult.addError(new FieldError("testObject", "name", "이름은 필수입니다"));
            bindingResult.addError(new FieldError("testObject", "email", "이메일 형식이 올바르지 않습니다"));

            MethodArgumentNotValidException exception =
                    new MethodArgumentNotValidException(null, bindingResult);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleValidationException(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getDetail()).isEqualTo("Validation failed for request");
            assertThat(response.getBody().getProperties()).containsKey("errors");
            assertThat(response.getBody().getInstance()).isEqualTo(URI.create("/api/test"));
        }

        @Test
        @DisplayName("필드 에러 정보를 errors 속성에 포함한다")
        @SuppressWarnings("unchecked")
        void shouldIncludeFieldErrorsInErrorsProperty() {
            // Given
            BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "testObject");
            bindingResult.addError(new FieldError("testObject", "name", "이름은 필수입니다"));

            MethodArgumentNotValidException exception =
                    new MethodArgumentNotValidException(null, bindingResult);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleValidationException(exception, mockRequest);

            // Then
            Map<String, String> errors =
                    (Map<String, String>) response.getBody().getProperties().get("errors");
            assertThat(errors).containsEntry("name", "이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("handleBindException()은")
    class HandleBindException {

        @Test
        @DisplayName("BindException을 400 BAD_REQUEST로 변환한다")
        void shouldReturn400ForBindException() {
            // Given
            BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "testObject");
            bindingResult.addError(new FieldError("testObject", "page", "페이지는 0 이상이어야 합니다"));

            BindException exception = new BindException(bindingResult);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleBindException(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getProperties()).containsKey("errors");
        }

        @Test
        @DisplayName("바인딩 에러 정보를 errors 속성에 포함한다")
        @SuppressWarnings("unchecked")
        void shouldIncludeBindingErrorsInErrorsProperty() {
            // Given
            BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "testObject");
            bindingResult.addError(new FieldError("testObject", "size", "사이즈는 100 이하여야 합니다"));

            BindException exception = new BindException(bindingResult);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleBindException(exception, mockRequest);

            // Then
            Map<String, String> errors =
                    (Map<String, String>) response.getBody().getProperties().get("errors");
            assertThat(errors).containsEntry("size", "사이즈는 100 이하여야 합니다");
        }
    }

    @Nested
    @DisplayName("handleConstraintViolation()은")
    class HandleConstraintViolation {

        @Test
        @DisplayName("ConstraintViolationException을 400 BAD_REQUEST로 변환한다")
        void shouldReturn400ForConstraintViolationException() {
            // Given
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolation<?> violation = createMockViolation("sellerId", "ID는 양수여야 합니다");
            violations.add(violation);

            ConstraintViolationException exception = new ConstraintViolationException(violations);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleConstraintViolation(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getProperties()).containsKey("errors");
        }

        @Test
        @DisplayName("제약 조건 위반 정보를 errors 속성에 포함한다")
        @SuppressWarnings("unchecked")
        void shouldIncludeConstraintViolationsInErrorsProperty() {
            // Given
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            violations.add(createMockViolation("minPrice", "최소 가격은 0 이상이어야 합니다"));

            ConstraintViolationException exception = new ConstraintViolationException(violations);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleConstraintViolation(exception, mockRequest);

            // Then
            Map<String, String> errors =
                    (Map<String, String>) response.getBody().getProperties().get("errors");
            assertThat(errors).containsEntry("minPrice", "최소 가격은 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("propertyPath가 null인 경우 unknown으로 처리한다")
        @SuppressWarnings("unchecked")
        void shouldHandleNullPropertyPath() {
            // Given
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
            given(violation.getPropertyPath()).willReturn(null);
            given(violation.getMessage()).willReturn("알 수 없는 에러");
            violations.add(violation);

            ConstraintViolationException exception = new ConstraintViolationException(violations);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleConstraintViolation(exception, mockRequest);

            // Then
            Map<String, String> errors =
                    (Map<String, String>) response.getBody().getProperties().get("errors");
            assertThat(errors).containsEntry("unknown", "알 수 없는 에러");
        }

        private ConstraintViolation<?> createMockViolation(String path, String message) {
            ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
            Path mockPath = Mockito.mock(Path.class);
            given(mockPath.toString()).willReturn(path);
            given(violation.getPropertyPath()).willReturn(mockPath);
            given(violation.getMessage()).willReturn(message);
            return violation;
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException()은")
    class HandleIllegalArgumentException {

        @Test
        @DisplayName("IllegalArgumentException을 400 BAD_REQUEST로 변환한다")
        void shouldReturn400ForIllegalArgumentException() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException("잘못된 파라미터입니다");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getDetail()).isEqualTo("잘못된 파라미터입니다");
        }

        @Test
        @DisplayName("메시지가 null인 경우 기본 메시지를 사용한다")
        void shouldUseDefaultMessageWhenNull() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException((String) null);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getDetail()).isEqualTo("Invalid argument");
        }
    }

    @Nested
    @DisplayName("handleHttpMessageNotReadable()은")
    class HandleHttpMessageNotReadable {

        @Test
        @DisplayName("HttpMessageNotReadableException을 400 BAD_REQUEST로 변환한다")
        void shouldReturn400ForHttpMessageNotReadableException() {
            // Given
            HttpMessageNotReadableException exception =
                    new HttpMessageNotReadableException("JSON parse error", (Throwable) null, null);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleHttpMessageNotReadable(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getDetail()).isEqualTo("잘못된 요청 형식입니다. JSON 형식을 확인해주세요.");
        }
    }

    @Nested
    @DisplayName("handleTypeMismatch()은")
    class HandleTypeMismatch {

        @Test
        @DisplayName("MethodArgumentTypeMismatchException을 400 BAD_REQUEST로 변환한다")
        void shouldReturn400ForTypeMismatchException() {
            // Given
            MethodArgumentTypeMismatchException exception =
                    new MethodArgumentTypeMismatchException(
                            "abc", Long.class, "sellerId", null, null);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleTypeMismatch(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getDetail())
                    .contains("sellerId")
                    .contains("abc")
                    .contains("Long");
        }

        @Test
        @DisplayName("required type이 null인 경우 기본 텍스트를 사용한다")
        void shouldUseDefaultTextWhenRequiredTypeIsNull() {
            // Given
            MethodArgumentTypeMismatchException exception =
                    new MethodArgumentTypeMismatchException("abc", null, "param", null, null);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleTypeMismatch(exception, mockRequest);

            // Then
            assertThat(response.getBody().getDetail()).contains("required type");
        }
    }

    @Nested
    @DisplayName("handleMissingParam()은")
    class HandleMissingParam {

        @Test
        @DisplayName("MissingServletRequestParameterException을 400 BAD_REQUEST로 변환한다")
        void shouldReturn400ForMissingParamException() {
            // Given
            MissingServletRequestParameterException exception =
                    new MissingServletRequestParameterException("sellerId", "Long");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleMissingParam(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getDetail()).contains("sellerId").contains("필수 파라미터");
        }
    }

    @Nested
    @DisplayName("handleNoResource()은")
    class HandleNoResource {

        @Test
        @DisplayName("NoResourceFoundException을 404 NOT_FOUND로 변환한다")
        void shouldReturn404ForNoResourceFoundException() {
            // Given
            NoResourceFoundException exception =
                    new NoResourceFoundException(HttpMethod.GET, "/api/unknown");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleNoResource(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Not Found");
            assertThat(response.getBody().getDetail()).isEqualTo("요청한 리소스를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("handleMethodNotAllowed()은")
    class HandleMethodNotAllowed {

        @Test
        @DisplayName("HttpRequestMethodNotSupportedException을 405 METHOD_NOT_ALLOWED로 변환한다")
        void shouldReturn405ForMethodNotSupportedException() {
            // Given
            HttpRequestMethodNotSupportedException exception =
                    new HttpRequestMethodNotSupportedException("DELETE", Set.of("GET", "POST"));

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleMethodNotAllowed(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Method Not Allowed");
            assertThat(response.getBody().getDetail()).contains("DELETE").contains("지원하지 않습니다");
        }

        @Test
        @DisplayName("지원되는 메서드가 없는 경우 '없음'을 표시한다")
        void shouldShowEmptyWhenNoSupportedMethods() {
            // Given
            HttpRequestMethodNotSupportedException exception =
                    new HttpRequestMethodNotSupportedException("DELETE");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleMethodNotAllowed(exception, mockRequest);

            // Then
            assertThat(response.getBody().getDetail()).contains("없음");
        }
    }

    @Nested
    @DisplayName("handleIllegalState()은")
    class HandleIllegalState {

        @Test
        @DisplayName("IllegalStateException을 409 CONFLICT로 변환한다")
        void shouldReturn409ForIllegalStateException() {
            // Given
            IllegalStateException exception = new IllegalStateException("현재 상태에서는 취소할 수 없습니다");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalState(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Conflict");
            assertThat(response.getBody().getDetail()).isEqualTo("현재 상태에서는 취소할 수 없습니다");
        }

        @Test
        @DisplayName("메시지가 null인 경우 기본 메시지를 사용한다")
        void shouldUseDefaultMessageWhenNull() {
            // Given
            IllegalStateException exception = new IllegalStateException((String) null);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalState(exception, mockRequest);

            // Then
            assertThat(response.getBody().getDetail()).isEqualTo("State conflict");
        }
    }

    @Nested
    @DisplayName("handleGlobal()은")
    class HandleGlobal {

        @Test
        @DisplayName("Exception을 500 INTERNAL_SERVER_ERROR로 변환한다")
        void shouldReturn500ForGenericException() {
            // Given
            Exception exception = new Exception("알 수 없는 오류");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleGlobal(exception, mockRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Internal Server Error");
            assertThat(response.getBody().getDetail()).isEqualTo("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @Nested
    @DisplayName("handleDomain()은")
    class HandleDomain {

        @Test
        @DisplayName("DomainException을 ErrorMapperRegistry를 통해 매핑한다")
        void shouldMapDomainExceptionUsingRegistry() {
            // Given
            DomainException exception = createDomainException("TEST-001", "테스트 에러");
            MappedError mappedError =
                    new MappedError(
                            HttpStatus.NOT_FOUND,
                            "리소스를 찾을 수 없습니다",
                            "요청한 리소스가 존재하지 않습니다",
                            URI.create("https://api.example.com/problems/test/test-001"));

            given(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .willReturn(Optional.of(mappedError));

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleDomain(exception, mockRequest, Locale.KOREA);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("리소스를 찾을 수 없습니다");
            assertThat(response.getBody().getDetail()).isEqualTo("요청한 리소스가 존재하지 않습니다");
            assertThat(response.getBody().getType())
                    .isEqualTo(URI.create("https://api.example.com/problems/test/test-001"));
            assertThat(response.getBody().getProperties()).containsEntry("code", "TEST-001");
        }

        @Test
        @DisplayName("ErrorMapper가 없으면 기본 매핑을 사용한다")
        void shouldUseDefaultMappingWhenNoMapper() {
            // Given
            DomainException exception = createDomainException("UNKNOWN-001", "알 수 없는 에러");
            MappedError defaultError =
                    new MappedError(
                            HttpStatus.BAD_REQUEST,
                            "Bad Request",
                            "알 수 없는 에러",
                            URI.create("about:blank"));

            given(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .willReturn(Optional.empty());
            given(errorMapperRegistry.defaultMapping(any(DomainException.class)))
                    .willReturn(defaultError);

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleDomain(exception, mockRequest, Locale.KOREA);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
        }

        @Test
        @DisplayName("args가 있으면 응답에 포함한다")
        void shouldIncludeArgsWhenPresent() {
            // Given
            DomainException exception =
                    createDomainExceptionWithArgs(
                            "TEST-002", "테스트 에러", Map.of("orderId", 123L, "reason", "취소됨"));
            MappedError mappedError =
                    new MappedError(HttpStatus.CONFLICT, "충돌", "상태 충돌", URI.create("about:blank"));

            given(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .willReturn(Optional.of(mappedError));

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleDomain(exception, mockRequest, Locale.KOREA);

            // Then
            assertThat(response.getBody().getProperties()).containsKey("args");
            @SuppressWarnings("unchecked")
            Map<String, Object> args =
                    (Map<String, Object>) response.getBody().getProperties().get("args");
            assertThat(args).containsEntry("orderId", 123L);
            assertThat(args).containsEntry("reason", "취소됨");
        }

        @Test
        @DisplayName("args가 비어있으면 응답에 포함하지 않는다")
        void shouldNotIncludeEmptyArgs() {
            // Given
            DomainException exception = createDomainException("TEST-003", "테스트 에러");
            MappedError mappedError =
                    new MappedError(
                            HttpStatus.BAD_REQUEST, "잘못된 요청", "에러 발생", URI.create("about:blank"));

            given(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .willReturn(Optional.of(mappedError));

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleDomain(exception, mockRequest, Locale.KOREA);

            // Then
            assertThat(response.getBody().getProperties()).doesNotContainKey("args");
        }

        private DomainException createDomainException(String code, String message) {
            ErrorCode errorCode = Mockito.mock(ErrorCode.class);
            given(errorCode.getCode()).willReturn(code);
            given(errorCode.getMessage()).willReturn(message);

            return new DomainException(errorCode, message) {
                @Override
                public String code() {
                    return code;
                }

                @Override
                public Map<String, Object> args() {
                    return Collections.emptyMap();
                }
            };
        }

        private DomainException createDomainExceptionWithArgs(
                String code, String message, Map<String, Object> args) {
            ErrorCode errorCode = Mockito.mock(ErrorCode.class);
            given(errorCode.getCode()).willReturn(code);
            given(errorCode.getMessage()).willReturn(message);

            return new DomainException(errorCode, message, args) {
                @Override
                public String code() {
                    return code;
                }

                @Override
                public Map<String, Object> args() {
                    return args;
                }
            };
        }
    }

    @Nested
    @DisplayName("공통 응답 속성")
    class CommonResponseProperties {

        @Test
        @DisplayName("timestamp 속성이 포함된다")
        void shouldIncludeTimestampProperty() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getProperties()).containsKey("timestamp");
        }

        @Test
        @DisplayName("instance에 요청 URI가 포함된다")
        void shouldIncludeRequestUriAsInstance() {
            // Given
            given(mockRequest.getRequestURI()).willReturn("/api/sellers/123");
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getInstance()).isEqualTo(URI.create("/api/sellers/123"));
        }

        @Test
        @DisplayName("쿼리 스트링이 있으면 instance에 포함된다")
        void shouldIncludeQueryStringInInstance() {
            // Given
            given(mockRequest.getRequestURI()).willReturn("/api/sellers");
            given(mockRequest.getQueryString()).willReturn("page=0&size=10");
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getInstance())
                    .isEqualTo(URI.create("/api/sellers?page=0&size=10"));
        }

        @Test
        @DisplayName("type은 기본적으로 about:blank이다")
        void shouldHaveAboutBlankAsDefaultType() {
            // Given
            IllegalArgumentException exception = new IllegalArgumentException("테스트");

            // When
            ResponseEntity<ProblemDetail> response =
                    globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

            // Then
            assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
        }
    }
}
