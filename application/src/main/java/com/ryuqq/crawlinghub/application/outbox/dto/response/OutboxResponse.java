package com.ryuqq.crawlinghub.application.outbox.dto.response;

import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;

/**
 * Outbox 응답 DTO
 *
 * @param crawlTaskId Task ID
 * @param idempotencyKey 멱등성 키
 * @param status 상태
 * @param retryCount 재시도 횟수
 * @param createdAt 생성 시각
 * @param processedAt 처리 시각
 * @author development-team
 * @since 1.0.0
 */
public record OutboxResponse(
        Long crawlTaskId,
        String idempotencyKey,
        OutboxStatus status,
        int retryCount,
        Instant createdAt,
        Instant processedAt) {}
