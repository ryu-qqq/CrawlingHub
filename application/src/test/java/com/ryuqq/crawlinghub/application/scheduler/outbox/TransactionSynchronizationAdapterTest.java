package com.ryuqq.crawlinghub.application.scheduler.outbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.ryuqq.crawlinghub.application.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.OutboxEventQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.fixture.outbox.TransactionSynchronizationAdapterFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("TransactionSynchronizationAdapter")
class TransactionSynchronizationAdapterTest {

    private final OutboxEventQueryPort outboxEventQueryPort = Mockito.mock(OutboxEventQueryPort.class);
    private final OutboxEventPersistencePort outboxEventPersistencePort = Mockito.mock(OutboxEventPersistencePort.class);
    private final EventBridgeClientPort eventBridgeClientPort = Mockito.mock(EventBridgeClientPort.class);
    private final Clock clock = Clock.systemUTC();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private TransactionSynchronizationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = TransactionSynchronizationAdapterFixture.create(
            outboxEventQueryPort,
            outboxEventPersistencePort,
            eventBridgeClientPort,
            clock,
            objectMapper
        );
    }

    @Test
    @DisplayName("should process outbox event after commit")
    void shouldProcessOutboxEventAfterCommit() {
        OutboxEvent pendingEvent = OutboxEvent.of(
            1L,
            OutboxEventType.SCHEDULER_CREATED,
            100L,
            "{\"cronExpression\":\"cron(0 0 * * ? *)\",\"target\":\"arn:aws:lambda:ap-northeast-2:123456789012:function:target\"}",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        );

        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.PENDING,
            3
        )).willReturn(List.of(pendingEvent));
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        adapter.processOutboxEvents();

        verify(eventBridgeClientPort).createRule(
            eq("scheduler-100"),
            eq("cron(0 0 * * ? *)"),
            eq("arn:aws:lambda:ap-northeast-2:123456789012:function:target")
        );
        verify(outboxEventPersistencePort).saveOutboxEvent(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("should not process when no pending events")
    void shouldNotProcessWhenNoPendingEvents() {
        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.PENDING,
            3
        )).willReturn(List.of());

        adapter.processOutboxEvents();

        verify(eventBridgeClientPort, never()).createRule(any(), any(), any());
        verify(eventBridgeClientPort, never()).updateRule(any(), any());
        verify(eventBridgeClientPort, never()).disableRule(any());
    }

    @Test
    @DisplayName("should create rule when scheduler created")
    void shouldCreateRuleWhenSchedulerCreated() {
        OutboxEvent createdEvent = OutboxEvent.of(
            1L,
            OutboxEventType.SCHEDULER_CREATED,
            100L,
            "{\"cronExpression\":\"cron(0 0 * * ? *)\",\"target\":\"arn:aws:lambda:ap-northeast-2:123456789012:function:target\"}",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        );

        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.PENDING,
            3
        )).willReturn(List.of(createdEvent));
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        adapter.processOutboxEvents();

        verify(eventBridgeClientPort).createRule(
            eq("scheduler-100"),
            eq("cron(0 0 * * ? *)"),
            eq("arn:aws:lambda:ap-northeast-2:123456789012:function:target")
        );
        verify(outboxEventPersistencePort).saveOutboxEvent(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("should update rule when scheduler updated")
    void shouldUpdateRuleWhenSchedulerUpdated() {
        OutboxEvent updatedEvent = OutboxEvent.of(
            2L,
            OutboxEventType.SCHEDULER_UPDATED,
            200L,
            "{\"cronExpression\":\"cron(0 1 * * ? *)\"}",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        );

        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.PENDING,
            3
        )).willReturn(List.of(updatedEvent));
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        adapter.processOutboxEvents();

        verify(eventBridgeClientPort).updateRule(
            eq("scheduler-200"),
            eq("cron(0 1 * * ? *)")
        );
        verify(outboxEventPersistencePort).saveOutboxEvent(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("should disable rule when scheduler deleted")
    void shouldDisableRuleWhenSchedulerDeleted() {
        OutboxEvent deletedEvent = OutboxEvent.of(
            3L,
            OutboxEventType.SCHEDULER_DELETED,
            300L,
            "{}",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        );

        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.PENDING,
            3
        )).willReturn(List.of(deletedEvent));
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        adapter.processOutboxEvents();

        verify(eventBridgeClientPort).disableRule(eq("scheduler-300"));
        verify(outboxEventPersistencePort).saveOutboxEvent(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("should mark as failed when AWS call fails")
    void shouldMarkAsFailedWhenAwsCallFails() {
        OutboxEvent createdEvent = OutboxEvent.of(
            1L,
            OutboxEventType.SCHEDULER_CREATED,
            100L,
            "{\"cronExpression\":\"cron(0 0 * * ? *)\",\"target\":\"arn:aws:lambda:ap-northeast-2:123456789012:function:target\"}",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        );

        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.PENDING,
            3
        )).willReturn(List.of(createdEvent));
        doThrow(new RuntimeException("AWS API call failed"))
            .when(eventBridgeClientPort).createRule(any(), any(), any());

        adapter.processOutboxEvents();

        verify(outboxEventPersistencePort).saveOutboxEvent(argThat(event -> {
            return event.status() == OutboxStatus.PENDING && event.retryCount() == 1;
        }));
    }

    @Test
    @DisplayName("should save error message when AWS call fails")
    void shouldSaveErrorMessage() {
        OutboxEvent createdEvent = OutboxEvent.of(
            1L,
            OutboxEventType.SCHEDULER_CREATED,
            100L,
            "{\"cronExpression\":\"cron(0 0 * * ? *)\",\"target\":\"arn:aws:lambda:ap-northeast-2:123456789012:function:target\"}",
            OutboxStatus.PENDING,
            2,
            3,
            LocalDateTime.now(),
            null,
            null
        );

        RuntimeException exception = new RuntimeException("AWS API call failed");
        given(outboxEventQueryPort.findByStatusAndRetryCountLessThan(
            OutboxStatus.PENDING,
            3
        )).willReturn(List.of(createdEvent));
        doThrow(exception)
            .when(eventBridgeClientPort).createRule(any(), any(), any());

        adapter.processOutboxEvents();

        verify(outboxEventPersistencePort).saveOutboxEvent(argThat(event -> {
            return event.status() == OutboxStatus.FAILED 
                && event.retryCount() == 3 
                && event.errorMessage() != null
                && event.errorMessage().contains("AWS API call failed");
        }));
    }
}

