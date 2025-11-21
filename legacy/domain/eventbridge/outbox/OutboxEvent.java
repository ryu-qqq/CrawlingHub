package com.ryuqq.crawlinghub.domain.eventbridge.outbox;

import java.time.LocalDateTime;

/** Scheduler Outbox Event Aggregate */
public record OutboxEvent(
        Long eventId,
        OutboxEventType eventType,
        Long schedulerId,
        String payload,
        OutboxStatus status,
        Integer retryCount,
        Integer maxRetries,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        String errorMessage) {

    public static OutboxEvent of(
            Long eventId,
            OutboxEventType eventType,
            Long schedulerId,
            String payload,
            OutboxStatus status,
            Integer retryCount,
            Integer maxRetries,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            String errorMessage) {
        return new OutboxEvent(
                eventId,
                eventType,
                schedulerId,
                payload,
                status,
                retryCount,
                maxRetries,
                createdAt,
                processedAt,
                errorMessage);
    }
}
