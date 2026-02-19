package com.ryuqq.crawlinghub.application.common.dto.result;

/**
 * 일괄 처리 시 개별 항목의 성공/실패 결과
 *
 * @param <T> ID 타입
 * @param id 항목 ID
 * @param success 성공 여부
 * @param errorCode 에러 코드 (실패 시)
 * @param errorMessage 에러 메시지 (실패 시)
 */
public record BatchItemResult<T>(T id, boolean success, String errorCode, String errorMessage) {

    public static <T> BatchItemResult<T> success(T id) {
        return new BatchItemResult<>(id, true, null, null);
    }

    public static <T> BatchItemResult<T> failure(T id, String errorCode, String errorMessage) {
        return new BatchItemResult<>(id, false, errorCode, errorMessage);
    }
}
