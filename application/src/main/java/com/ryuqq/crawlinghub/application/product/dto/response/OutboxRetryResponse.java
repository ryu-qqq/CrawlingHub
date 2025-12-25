package com.ryuqq.crawlinghub.application.product.dto.response;

/**
 * Outbox 재시도 응답
 *
 * @param outboxId Outbox ID
 * @param previousStatus 이전 상태
 * @param newStatus 새 상태
 * @param message 결과 메시지
 */
public record OutboxRetryResponse(
        Long outboxId, String previousStatus, String newStatus, String message) {

    public static OutboxRetryResponse success(
            Long outboxId, String previousStatus, String newStatus) {
        return new OutboxRetryResponse(outboxId, previousStatus, newStatus, "재시도 요청이 등록되었습니다.");
    }
}
