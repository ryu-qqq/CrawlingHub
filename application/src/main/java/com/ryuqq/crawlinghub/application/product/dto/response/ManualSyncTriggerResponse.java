package com.ryuqq.crawlinghub.application.product.dto.response;

/**
 * 수동 동기화 트리거 응답
 *
 * @param crawledProductId CrawledProduct ID
 * @param syncOutboxId 생성된 SyncOutbox ID
 * @param syncType 동기화 타입 (CREATE/UPDATE)
 * @param message 결과 메시지
 */
public record ManualSyncTriggerResponse(
        Long crawledProductId, Long syncOutboxId, String syncType, String message) {

    public static ManualSyncTriggerResponse success(
            Long crawledProductId, Long syncOutboxId, String syncType) {
        return new ManualSyncTriggerResponse(
                crawledProductId, syncOutboxId, syncType, "동기화 요청이 등록되었습니다.");
    }

    /**
     * 중복 요청으로 스킵된 경우
     *
     * @param crawledProductId CrawledProduct ID
     * @param reason 스킵 사유
     * @return 스킵 응답
     */
    public static ManualSyncTriggerResponse skipped(Long crawledProductId, String reason) {
        return new ManualSyncTriggerResponse(crawledProductId, null, null, reason);
    }
}
