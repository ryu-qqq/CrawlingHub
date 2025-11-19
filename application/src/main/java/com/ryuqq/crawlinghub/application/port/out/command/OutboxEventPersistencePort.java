package com.ryuqq.crawlinghub.application.port.out.command;

import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import java.time.LocalDateTime;

/**
 * OutboxEvent Persistence Port
 */
public interface OutboxEventPersistencePort {

    OutboxEvent saveOutboxEvent(OutboxEvent event);

    void deleteOutboxEvents(OutboxStatus status, LocalDateTime before);
}

