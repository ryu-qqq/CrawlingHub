package com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response;

/**
 * 수동 동기화 트리거 API 응답
 *
 * @param crawledProductId CrawledProduct ID
 * @param syncOutboxId 생성된 SyncOutbox ID
 * @param syncType 동기화 타입 (CREATE/UPDATE)
 * @param message 결과 메시지
 */
public record ManualSyncTriggerApiResponse(
        Long crawledProductId, Long syncOutboxId, String syncType, String message) {}
