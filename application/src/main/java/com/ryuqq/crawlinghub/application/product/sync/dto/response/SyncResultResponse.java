package com.ryuqq.crawlinghub.application.product.sync.dto.response;

import java.time.LocalDateTime;

/**
 * 동기화 결과 Response
 *
 * @param productId 상품 ID
 * @param success 성공 여부
 * @param syncedAt 동기화 시간
 * @param error 에러 메시지 (실패 시)
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record SyncResultResponse(
    Long productId,
    boolean success,
    LocalDateTime syncedAt,
    String error
) {
    /**
     * 성공 응답 생성
     */
    public static SyncResultResponse success(Long productId, LocalDateTime syncedAt) {
        return new SyncResultResponse(productId, true, syncedAt, null);
    }

    /**
     * 실패 응답 생성
     */
    public static SyncResultResponse failure(Long productId, String error) {
        return new SyncResultResponse(productId, false, LocalDateTime.now(), error);
    }
}
