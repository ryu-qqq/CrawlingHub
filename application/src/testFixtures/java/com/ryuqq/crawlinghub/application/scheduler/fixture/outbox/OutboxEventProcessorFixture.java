package com.ryuqq.crawlinghub.application.scheduler.fixture.outbox;

import com.ryuqq.crawlinghub.application.port.out.client.SlackClientPort;
import com.ryuqq.crawlinghub.application.port.out.query.OutboxEventQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.outbox.OutboxEventProcessor;
import com.ryuqq.crawlinghub.application.scheduler.outbox.TransactionSynchronizationAdapter;

public final class OutboxEventProcessorFixture {

    private OutboxEventProcessorFixture() {
    }

    public static OutboxEventProcessor create(
        TransactionSynchronizationAdapter transactionSynchronizationAdapter,
        OutboxEventQueryPort outboxEventQueryPort,
        SlackClientPort slackClientPort
    ) {
        return new OutboxEventProcessor(
            transactionSynchronizationAdapter,
            outboxEventQueryPort,
            slackClientPort
        );
    }
}

