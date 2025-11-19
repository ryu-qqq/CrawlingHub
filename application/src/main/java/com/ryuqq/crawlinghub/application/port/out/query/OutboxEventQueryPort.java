package com.ryuqq.crawlinghub.application.port.out.query;

import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OutboxEvent Query Port
 */
public interface OutboxEventQueryPort {

    List<OutboxEvent> findByStatusAndRetryCountLessThan(OutboxStatus status, Integer maxRetries);

    List<OutboxEvent> findByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime before);
}

