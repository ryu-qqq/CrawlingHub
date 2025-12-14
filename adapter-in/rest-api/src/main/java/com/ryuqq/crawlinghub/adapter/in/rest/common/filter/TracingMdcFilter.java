package com.ryuqq.crawlinghub.adapter.in.rest.common.filter;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Tracing MDC Filter
 *
 * <p>HTTP 요청에 대해 traceId, spanId, requestId를 MDC에 설정하여 로그에서 분산 추적 컨텍스트를 확인할 수 있게 합니다.
 *
 * <h3>MDC에 설정되는 키</h3>
 *
 * <ul>
 *   <li>traceId: OpenTelemetry 트레이스 ID (또는 생성된 UUID)
 *   <li>spanId: OpenTelemetry 스팬 ID
 *   <li>requestId: X-Request-Id 헤더 또는 생성된 짧은 ID
 * </ul>
 *
 * <h3>로그 출력 예시</h3>
 *
 * <pre>{@code
 * {
 *   "@timestamp": "2024-01-15T10:30:45.123+0900",
 *   "level": "INFO",
 *   "message": "Request processed successfully",
 *   "traceId": "abc123def456789",
 *   "spanId": "xyz789",
 *   "requestId": "req-a1b2c3"
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TracingMdcFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";
    private static final String REQUEST_ID = "requestId";
    private static final String X_REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            setupMdc(request);
            filterChain.doFilter(request, response);
        } finally {
            clearMdc();
        }
    }

    private void setupMdc(HttpServletRequest request) {
        // Request ID 설정 (X-Request-Id 헤더 또는 생성)
        String requestId = request.getHeader(X_REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = generateShortId();
        }
        MDC.put(REQUEST_ID, requestId);

        // OpenTelemetry에서 traceId, spanId 가져오기
        // ADOT Agent 사용 시 자동으로 설정됨
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
            MDC.put(TRACE_ID, currentSpan.getSpanContext().getTraceId());
            MDC.put(SPAN_ID, currentSpan.getSpanContext().getSpanId());
        } else {
            // OpenTelemetry가 없는 경우 생성된 ID 사용
            MDC.put(TRACE_ID, generateTraceId());
        }
    }

    private void clearMdc() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(REQUEST_ID);
    }

    private String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
