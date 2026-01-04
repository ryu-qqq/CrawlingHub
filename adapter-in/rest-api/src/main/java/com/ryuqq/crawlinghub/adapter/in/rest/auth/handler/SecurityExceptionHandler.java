package com.ryuqq.crawlinghub.adapter.in.rest.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Security 예외 핸들러
 *
 * <p>인증 실패(401) 및 접근 거부(403) 예외를 RFC 7807 ProblemDetail 형식으로 응답합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 실패 처리 (401 Unauthorized)
     *
     * <p>JWT 토큰이 없거나 유효하지 않은 경우 호출됩니다.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {

        writeErrorResponse(
                request, response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다.");
    }

    /**
     * 접근 거부 처리 (403 Forbidden)
     *
     * <p>인증은 되었지만 권한이 없는 경우 호출됩니다.
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {

        writeErrorResponse(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.");
    }

    /** RFC 7807 ProblemDetail 형식으로 에러 응답 작성 */
    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String detail)
            throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // RFC 7807 ProblemDetail을 Map으로 직렬화 (Spring의 ProblemDetail은 properties 직렬화 이슈가 있을 수 있음)
        Map<String, Object> problemDetail = new LinkedHashMap<>();
        problemDetail.put("type", "about:blank");
        problemDetail.put("title", status.getReasonPhrase());
        problemDetail.put("status", status.value());
        problemDetail.put("detail", detail);
        problemDetail.put("code", code);
        problemDetail.put("timestamp", DateTimeFormatUtils.nowIso8601());

        // 요청 경로를 instance로
        String uri = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            uri = uri + "?" + request.getQueryString();
        }
        problemDetail.put("instance", uri);

        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
