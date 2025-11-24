package com.ryuqq.crawlinghub.application.schedule.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.OutboxEventQueryPort;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TransactionSynchronizationAdapter {

    private static final int MAX_RETRIES = 3;

    private final OutboxEventQueryPort outboxEventQueryPort;
    private final OutboxEventPersistencePort outboxEventPersistencePort;
    private final EventBridgeClientPort eventBridgeClientPort;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public TransactionSynchronizationAdapter(
            OutboxEventQueryPort outboxEventQueryPort,
            OutboxEventPersistencePort outboxEventPersistencePort,
            EventBridgeClientPort eventBridgeClientPort,
            Clock clock,
            ObjectMapper objectMapper) {
        this.outboxEventQueryPort = outboxEventQueryPort;
        this.outboxEventPersistencePort = outboxEventPersistencePort;
        this.eventBridgeClientPort = eventBridgeClientPort;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Async
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents =
                outboxEventQueryPort.findByStatusAndRetryCountLessThan(
                        OutboxStatus.PENDING, MAX_RETRIES);

        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                handleFailure(event, e);
            }
        }
    }

    private void processEvent(OutboxEvent event) {
        OutboxEventType eventType = event.eventType();
        Long schedulerId = event.schedulerId();
        String ruleName = generateRuleName(schedulerId);

        switch (eventType) {
            case SCHEDULER_CREATED:
                SchedulerCreatedPayload createdPayload =
                        parsePayload(event.payload(), SchedulerCreatedPayload.class);
                eventBridgeClientPort.createRule(
                        ruleName, createdPayload.cronExpression(), createdPayload.target());
                break;
            case SCHEDULER_UPDATED:
                SchedulerUpdatedPayload updatedPayload =
                        parsePayload(event.payload(), SchedulerUpdatedPayload.class);
                eventBridgeClientPort.updateRule(ruleName, updatedPayload.cronExpression());
                break;
            case SCHEDULER_DELETED:
                eventBridgeClientPort.disableRule(ruleName);
                break;
        }

        OutboxEvent published =
                OutboxEvent.of(
                        event.eventId(),
                        event.eventType(),
                        event.schedulerId(),
                        event.payload(),
                        OutboxStatus.PUBLISHED,
                        event.retryCount(),
                        event.maxRetries(),
                        event.createdAt(),
                        LocalDateTime.now(clock),
                        null);

        outboxEventPersistencePort.saveOutboxEvent(published);
    }

    private <T> T parsePayload(String payload, Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse payload: " + payload, e);
        }
    }

    private record SchedulerCreatedPayload(String cronExpression, String target) {}

    private record SchedulerUpdatedPayload(String cronExpression) {}

    private void handleFailure(OutboxEvent event, Exception e) {
        int newRetryCount = event.retryCount() + 1;
        OutboxStatus newStatus =
                newRetryCount >= event.maxRetries() ? OutboxStatus.FAILED : OutboxStatus.PENDING;

        OutboxEvent failed =
                OutboxEvent.of(
                        event.eventId(),
                        event.eventType(),
                        event.schedulerId(),
                        event.payload(),
                        newStatus,
                        newRetryCount,
                        event.maxRetries(),
                        event.createdAt(),
                        event.processedAt(),
                        e.getMessage());

        outboxEventPersistencePort.saveOutboxEvent(failed);
    }

    private String generateRuleName(Long schedulerId) {
        return "scheduler-" + schedulerId;
    }
}
