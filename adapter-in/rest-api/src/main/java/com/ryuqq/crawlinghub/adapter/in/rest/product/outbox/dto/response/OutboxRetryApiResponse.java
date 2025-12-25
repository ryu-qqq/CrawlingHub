package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response;

/**
 * Outbox 재시도 API 응답
 *
 * @param outboxId Outbox ID
 * @param previousStatus 이전 상태
 * @param newStatus 새 상태
 * @param message 결과 메시지
 */
public record OutboxRetryApiResponse(
        Long outboxId, String previousStatus, String newStatus, String message) {}
