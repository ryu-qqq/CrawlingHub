package com.ryuqq.crawlinghub.application.scheduler.fixture.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.OutboxEventQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.outbox.TransactionSynchronizationAdapter;
import java.time.Clock;

public final class TransactionSynchronizationAdapterFixture {

    private TransactionSynchronizationAdapterFixture() {
    }

    public static TransactionSynchronizationAdapter create(
        OutboxEventQueryPort outboxEventQueryPort,
        OutboxEventPersistencePort outboxEventPersistencePort,
        EventBridgeClientPort eventBridgeClientPort,
        Clock clock,
        ObjectMapper objectMapper
    ) {
        return new TransactionSynchronizationAdapter(
            outboxEventQueryPort,
            outboxEventPersistencePort,
            eventBridgeClientPort,
            clock,
            objectMapper
        );
    }
}

