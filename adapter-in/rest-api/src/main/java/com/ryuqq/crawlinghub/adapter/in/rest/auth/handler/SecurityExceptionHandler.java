package com.ryuqq.crawlinghub.adapter.in.rest.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
 * <p>인증 실패(401) 및 접근 거부(403) 예외를 JSON 형식으로 응답합니다.
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
                response, HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다.", authException);
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

        writeErrorResponse(
                response, HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다.", accessDeniedException);
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String errorCode,
            String message,
            Exception exception)
            throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.ofFailure(errorCode, message);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
