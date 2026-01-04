package com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response;

import com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils;
import java.util.UUID;

/**
 * ApiResponse - 표준 API 응답 래퍼
 *
 * <p>모든 REST API 성공 응답의 일관된 형식을 제공합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // 성공 응답 (데이터 포함)
 * ApiResponse<UserDto> response = ApiResponse.ofSuccess(userDto);
 *
 * // 성공 응답 (데이터 없음)
 * ApiResponse<Void> response = ApiResponse.ofSuccess();
 * }</pre>
 *
 * <p><strong>응답 형식:</strong>
 *
 * <pre>{@code
 * {
 *   "data": { ... },
 *   "timestamp": "2025-12-22T10:30:00",
 *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
 * }
 * }</pre>
 *
 * <p><strong>에러 응답:</strong> 에러 응답은 RFC 7807 ProblemDetail 형식을 사용합니다. GlobalExceptionHandler 및
 * SecurityExceptionHandler를 참조하세요.
 *
 * @param <T> 응답 데이터 타입
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record ApiResponse<T>(T data, String timestamp, String requestId) {

    /**
     * 성공 응답 생성
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<>(data, DateTimeFormatUtils.nowIso8601(), generateRequestId());
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     *
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static <T> ApiResponse<T> ofSuccess() {
        return ofSuccess(null);
    }

    /**
     * Request ID 생성
     *
     * <p>UUID v4 형식으로 생성합니다.
     *
     * @return UUID 형식의 Request ID
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
