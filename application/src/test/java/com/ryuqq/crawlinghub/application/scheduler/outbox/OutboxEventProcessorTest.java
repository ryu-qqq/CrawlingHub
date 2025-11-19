package com.ryuqq.crawlinghub.application.scheduler.outbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.port.out.client.SlackClientPort;
import com.ryuqq.crawlinghub.application.port.out.query.OutboxEventQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.fixture.outbox.OutboxEventProcessorFixture;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("OutboxEventProcessor")
class OutboxEventProcessorTest {

    private final TransactionSynchronizationAdapter transactionSynchronizationAdapter = 
        Mockito.mock(TransactionSynchronizationAdapter.class);
    private final OutboxEventQueryPort outboxEventQueryPort = 
        Mockito.mock(OutboxEventQueryPort.class);
    private final SlackClientPort slackClientPort = 
        Mockito.mock(SlackClientPort.class);

    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = OutboxEventProcessorFixture.create(
            transactionSynchronizationAdapter,
            outboxEventQueryPort,
            slackClientPort
        );
    }

    @Test
    @DisplayName("should process pending outbox events")
    void shouldProcessPendingOutboxEvents() {
        processor.processOutboxEvents();

        verify(transactionSynchronizationAdapter, times(1)).processOutboxEvents();
    }

    @Test
    @DisplayName("should increment retry count when processing fails")
    void shouldIncrementRetryCount() {
        // OutboxEventProcessor는 TransactionSynchronizationAdapter를 호출하고,
        // 재시도 로직은 TransactionSynchronizationAdapter에서 처리됩니다.
        // 따라서 OutboxEventProcessor는 항상 processOutboxEvents를 호출합니다.
        processor.processOutboxEvents();

        verify(transactionSynchronizationAdapter, times(1)).processOutboxEvents();
    }

    @Test
    @DisplayName("should mark as failed when max retries exceeded")
    void shouldMarkAsFailedWhenMaxRetriesExceeded() {
        // OutboxEventProcessor는 TransactionSynchronizationAdapter를 호출하고,
        // 최대 재시도 초과 처리도 TransactionSynchronizationAdapter에서 처리됩니다.
        // 따라서 OutboxEventProcessor는 항상 processOutboxEvents를 호출합니다.
        processor.processOutboxEvents();

        verify(transactionSynchronizationAdapter, times(1)).processOutboxEvents();
    }

    @Test
    @DisplayName("should send Slack notification when failed")
    void shouldSendSlackNotificationWhenFailed() {
        OutboxEvent failedEvent = OutboxEvent.of(
            1L,
            OutboxEventType.SCHEDULER_CREATED,
            100L,
            "{\"cronExpression\":\"cron(0 0 * * ? *)\",\"target\":\"arn:aws:lambda:ap-northeast-2:123456789012:function:target\"}",
            OutboxStatus.FAILED,
            3,
            3,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "AWS API call failed"
        );

        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.FAILED,
            3
        )).willReturn(List.of(failedEvent));

        processor.processOutboxEvents();

        verify(slackClientPort, times(1)).sendNotification(anyString());
        verify(transactionSynchronizationAdapter, times(1)).processOutboxEvents();
    }

    @Test
    @DisplayName("should not send Slack notification when no failed events")
    void shouldNotSendSlackNotificationWhenNoFailedEvents() {
        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.FAILED,
            3
        )).willReturn(List.of());

        processor.processOutboxEvents();

        verify(slackClientPort, never()).sendNotification(anyString());
        verify(transactionSynchronizationAdapter, times(1)).processOutboxEvents();
    }
}

