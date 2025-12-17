package com.ryuqq.crawlinghub.adapter.out.fileflow.dto;

import java.time.LocalDateTime;

/**
 * Fileflow API 응답 래퍼 DTO
 *
 * <p>Fileflow API의 표준 응답 형식입니다.
 *
 * @param success 성공 여부
 * @param data 응답 데이터
 * @param error 에러 정보
 * @param timestamp 응답 시간
 * @param requestId 요청 ID
 * @author development-team
 * @since 1.0.0
 */
public record FileflowApiResponse<T>(
        boolean success,
        T data,
        FileflowErrorInfo error,
        LocalDateTime timestamp,
        String requestId) {

    public boolean isSuccess() {
        return success;
    }

    public boolean hasData() {
        return data != null;
    }

    /**
     * Fileflow 에러 정보 DTO
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    public record FileflowErrorInfo(String errorCode, String message) {}
}
