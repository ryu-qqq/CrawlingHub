package com.ryuqq.crawlinghub.application.scheduler.outbox;

import com.ryuqq.crawlinghub.application.port.out.client.SlackClientPort;
import com.ryuqq.crawlinghub.application.port.out.query.OutboxEventQueryPort;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventProcessor {

    private static final int MAX_RETRIES = 3;

    private final TransactionSynchronizationAdapter transactionSynchronizationAdapter;
    private final OutboxEventQueryPort outboxEventQueryPort;
    private final SlackClientPort slackClientPort;

    public OutboxEventProcessor(
        TransactionSynchronizationAdapter transactionSynchronizationAdapter,
        OutboxEventQueryPort outboxEventQueryPort,
        SlackClientPort slackClientPort
    ) {
        this.transactionSynchronizationAdapter = transactionSynchronizationAdapter;
        this.outboxEventQueryPort = outboxEventQueryPort;
        this.slackClientPort = slackClientPort;
    }

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void processOutboxEvents() {
        transactionSynchronizationAdapter.processOutboxEvents();
        sendSlackNotificationsForFailedEvents();
    }

    private void sendSlackNotificationsForFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.FAILED,
            MAX_RETRIES
        );

        for (OutboxEvent event : failedEvents) {
            String message = String.format(
                "⚠️ OutboxEvent 처리 실패: schedulerId=%d, eventType=%s, errorMessage=%s",
                event.schedulerId(),
                event.eventType(),
                event.errorMessage()
            );
            slackClientPort.sendNotification(message);
        }
    }
}

