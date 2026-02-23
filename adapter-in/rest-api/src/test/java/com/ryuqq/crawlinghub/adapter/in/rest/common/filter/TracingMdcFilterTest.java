package com.ryuqq.crawlinghub.adapter.in.rest.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * TracingMdcFilter 단위 테스트
 *
 * <p>HTTP 요청에 대한 traceId, spanId, requestId의 MDC 설정 및 정리 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("TracingMdcFilter 단위 테스트")
class TracingMdcFilterTest {

    private TracingMdcFilter filter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockFilterChain;

    @BeforeEach
    void setUp() {
        filter = new TracingMdcFilter();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("requestId 설정")
    class RequestIdSetupTest {

        @Test
        @DisplayName("X-Request-Id 헤더가 있으면 MDC에 해당 값을 requestId로 설정한다")
        void shouldUseXRequestIdHeaderWhenPresent() throws Exception {
            // Given
            String requestId = "custom-request-id-123";
            given(mockRequest.getHeader("X-Request-Id")).willReturn(requestId);

            // MDC에 값을 캡처하기 위해 FilterChain 실행 시점의 MDC를 확인
            final String[] capturedRequestId = {null};
            org.mockito.Mockito.doAnswer(
                            invocation -> {
                                capturedRequestId[0] = MDC.get("requestId");
                                return null;
                            })
                    .when(mockFilterChain)
                    .doFilter(mockRequest, mockResponse);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            assertThat(capturedRequestId[0]).isEqualTo(requestId);
        }

        @Test
        @DisplayName("X-Request-Id 헤더가 없으면 UUID로 requestId를 생성한다")
        void shouldGenerateUuidRequestIdWhenHeaderAbsent() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn(null);

            final String[] capturedRequestId = {null};
            org.mockito.Mockito.doAnswer(
                            invocation -> {
                                capturedRequestId[0] = MDC.get("requestId");
                                return null;
                            })
                    .when(mockFilterChain)
                    .doFilter(mockRequest, mockResponse);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            // 생성된 requestId는 UUID의 앞 8자리 (substring(0, 8))
            assertThat(capturedRequestId[0]).isNotNull();
            assertThat(capturedRequestId[0]).hasSize(8);
        }

        @Test
        @DisplayName("X-Request-Id 헤더가 공백이면 UUID로 requestId를 생성한다")
        void shouldGenerateUuidRequestIdWhenHeaderIsBlank() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn("   ");

            final String[] capturedRequestId = {null};
            org.mockito.Mockito.doAnswer(
                            invocation -> {
                                capturedRequestId[0] = MDC.get("requestId");
                                return null;
                            })
                    .when(mockFilterChain)
                    .doFilter(mockRequest, mockResponse);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            assertThat(capturedRequestId[0]).isNotNull();
            assertThat(capturedRequestId[0]).hasSize(8);
        }
    }

    @Nested
    @DisplayName("traceId 설정 (OpenTelemetry 없는 환경)")
    class TraceIdSetupTest {

        @Test
        @DisplayName("OpenTelemetry span이 없으면 생성된 traceId를 MDC에 설정한다")
        void shouldSetGeneratedTraceIdWhenNoOpenTelemetrySpan() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");

            final String[] capturedTraceId = {null};
            org.mockito.Mockito.doAnswer(
                            invocation -> {
                                capturedTraceId[0] = MDC.get("traceId");
                                return null;
                            })
                    .when(mockFilterChain)
                    .doFilter(mockRequest, mockResponse);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            // OpenTelemetry span이 없는 테스트 환경에서는 생성된 traceId가 MDC에 설정됨
            // UUID에서 하이픈을 제거한 32자리 hex 문자열
            assertThat(capturedTraceId[0]).isNotNull();
            assertThat(capturedTraceId[0]).matches("[0-9a-f]{32}");
        }
    }

    @Nested
    @DisplayName("traceId 설정 (OpenTelemetry 있는 환경)")
    class TraceIdWithOpenTelemetryTest {

        @Test
        @DisplayName("유효한 OpenTelemetry span이 있으면 traceId와 spanId를 MDC에 설정한다")
        void shouldSetTraceIdAndSpanIdFromOpenTelemetrySpan() throws Exception {
            // Given
            // 유효한 SpanContext 생성 (32자리 traceId, 16자리 spanId)
            String expectedTraceId = "0af7651916cd43dd8448eb211c80319c";
            String expectedSpanId = "b7ad6b7169203331";

            SpanContext spanContext =
                    SpanContext.create(
                            expectedTraceId,
                            expectedSpanId,
                            TraceFlags.getSampled(),
                            TraceState.getDefault());

            Span span = Span.wrap(spanContext);
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");

            final String[] capturedTraceId = {null};
            final String[] capturedSpanId = {null};

            org.mockito.Mockito.doAnswer(
                            invocation -> {
                                capturedTraceId[0] = MDC.get("traceId");
                                capturedSpanId[0] = MDC.get("spanId");
                                return null;
                            })
                    .when(mockFilterChain)
                    .doFilter(mockRequest, mockResponse);

            // OpenTelemetry Context에 span을 설정하고 현재 컨텍스트로 만들기
            try (Scope scope = Context.current().with(span).makeCurrent()) {
                // When
                filter.doFilter(mockRequest, mockResponse, mockFilterChain);
            }

            // Then
            assertThat(capturedTraceId[0]).isEqualTo(expectedTraceId);
            assertThat(capturedSpanId[0]).isEqualTo(expectedSpanId);
        }

        @Test
        @DisplayName("유효한 span이 있을 때 필터 처리 후 MDC가 정리된다")
        void shouldClearMdcAfterFilterWithValidSpan() throws Exception {
            // Given
            String expectedTraceId = "0af7651916cd43dd8448eb211c80319c";
            String expectedSpanId = "b7ad6b7169203331";

            SpanContext spanContext =
                    SpanContext.create(
                            expectedTraceId,
                            expectedSpanId,
                            TraceFlags.getSampled(),
                            TraceState.getDefault());

            Span span = Span.wrap(spanContext);
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");

            try (Scope scope = Context.current().with(span).makeCurrent()) {
                // When
                filter.doFilter(mockRequest, mockResponse, mockFilterChain);
            }

            // Then
            assertThat(MDC.get("traceId")).isNull();
            assertThat(MDC.get("spanId")).isNull();
        }
    }

    @Nested
    @DisplayName("MDC 정리 검증")
    class MdcClearTest {

        @Test
        @DisplayName("필터 처리 완료 후 MDC에서 traceId가 제거된다")
        void shouldClearTraceIdAfterFilter() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            assertThat(MDC.get("traceId")).isNull();
        }

        @Test
        @DisplayName("필터 처리 완료 후 MDC에서 spanId가 제거된다")
        void shouldClearSpanIdAfterFilter() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            assertThat(MDC.get("spanId")).isNull();
        }

        @Test
        @DisplayName("필터 처리 완료 후 MDC에서 requestId가 제거된다")
        void shouldClearRequestIdAfterFilter() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            assertThat(MDC.get("requestId")).isNull();
        }

        @Test
        @DisplayName("필터 체인에서 예외가 발생해도 MDC가 정리된다")
        void shouldClearMdcEvenWhenFilterChainThrowsException() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");
            org.mockito.Mockito.doThrow(new RuntimeException("테스트 예외"))
                    .when(mockFilterChain)
                    .doFilter(mockRequest, mockResponse);

            // When
            try {
                filter.doFilter(mockRequest, mockResponse, mockFilterChain);
            } catch (RuntimeException ignored) {
                // 예외는 무시
            }

            // Then - finally 블록에서 MDC가 정리되어야 함
            assertThat(MDC.get("traceId")).isNull();
            assertThat(MDC.get("requestId")).isNull();
        }
    }

    @Nested
    @DisplayName("필터 체인 진행 검증")
    class FilterChainTest {

        @Test
        @DisplayName("필터 처리 완료 후 filterChain.doFilter가 호출된다")
        void shouldCallFilterChainDoFilter() throws Exception {
            // Given
            given(mockRequest.getHeader("X-Request-Id")).willReturn("test-req");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        }
    }
}
