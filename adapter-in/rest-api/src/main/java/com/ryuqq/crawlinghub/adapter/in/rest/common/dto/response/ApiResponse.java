package com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response;

import com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * ApiResponse - 표준 API 응답 래퍼
 *
 * <p>모든 REST API 성공 응답의 일관된 형식을 제공합니다.
 *
 * @param <T> 응답 데이터 타입
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Schema(description = "표준 API 응답 래퍼")
public record ApiResponse<T>(
        @Schema(description = "응답 데이터") T data,
        @Schema(description = "응답 시각 (ISO 8601)", example = "2025-12-22T10:30:00+09:00")
                String timestamp,
        @Schema(
                        description = "요청 추적 ID (MDC traceId 우선, 없으면 UUID)",
                        example = "550e8400-e29b-41d4-a716-446655440000")
                String requestId) {

    /**
     * 성공 응답 생성
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse
     */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, DateTimeFormatUtils.nowIso8601(), resolveRequestId());
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     *
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse
     */
    public static <T> ApiResponse<T> of() {
        return of(null);
    }

    private static String resolveRequestId() {
        String traceId = MDC.get("traceId");
        return (traceId != null && !traceId.isBlank()) ? traceId : UUID.randomUUID().toString();
    }
}
