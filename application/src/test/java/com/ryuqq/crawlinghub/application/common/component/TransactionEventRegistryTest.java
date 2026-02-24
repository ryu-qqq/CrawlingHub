package com.ryuqq.crawlinghub.application.common.component;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * TransactionEventRegistry 단위 테스트
 *
 * <p>트랜잭션 컨텍스트 유/무에 따른 이벤트 발행 동작 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionEventRegistry 테스트")
class TransactionEventRegistryTest {

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private TransactionEventRegistry registry;

    /** 테스트용 DomainEvent 구현체 */
    static class TestDomainEvent implements DomainEvent {
        private final String name;
        private final Instant occurredAt;

        TestDomainEvent(String name) {
            this.name = name;
            this.occurredAt = Instant.now();
        }

        @Override
        public Instant occurredAt() {
            return occurredAt;
        }
    }

    @Nested
    @DisplayName("registerForPublish() 테스트")
    class RegisterForPublish {

        @Test
        @DisplayName("[성공] 트랜잭션 컨텍스트 없을 때 즉시 이벤트 발행")
        void shouldPublishImmediatelyWhenNoTransactionActive() {
            // Given
            TestDomainEvent event = new TestDomainEvent("test-event");

            try (MockedStatic<TransactionSynchronizationManager> mocked =
                    Mockito.mockStatic(TransactionSynchronizationManager.class)) {
                mocked.when(TransactionSynchronizationManager::isSynchronizationActive)
                        .thenReturn(false);

                // When
                registry.registerForPublish(event);

                // Then
                then(eventPublisher).should().publishEvent(event);
            }
        }

        @Test
        @DisplayName("[성공] 트랜잭션 컨텍스트 있을 때 즉시 발행하지 않음")
        void shouldNotPublishImmediatelyWhenTransactionActive() {
            // Given
            TestDomainEvent event = new TestDomainEvent("test-event");

            try (MockedStatic<TransactionSynchronizationManager> mocked =
                    Mockito.mockStatic(TransactionSynchronizationManager.class)) {
                mocked.when(TransactionSynchronizationManager::isSynchronizationActive)
                        .thenReturn(true);
                // registerSynchronization 호출 허용 (void 메서드)
                mocked.when(
                                () ->
                                        TransactionSynchronizationManager.registerSynchronization(
                                                Mockito.any(TransactionSynchronization.class)))
                        .then(invocation -> null);

                // When
                registry.registerForPublish(event);

                // Then - 즉시 발행 없음
                then(eventPublisher).should(never()).publishEvent(event);
            }
        }
    }

    @Nested
    @DisplayName("registerAllForPublish() 테스트")
    class RegisterAllForPublish {

        @Test
        @DisplayName("[성공] 빈 목록은 무시")
        void shouldIgnoreEmptyList() {
            // When
            registry.registerAllForPublish(List.of());

            // Then - publisher 호출 없음
            then(eventPublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[성공] null 목록은 무시")
        void shouldIgnoreNullList() {
            // When
            registry.registerAllForPublish(null);

            // Then - publisher 호출 없음
            then(eventPublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[성공] 트랜잭션 없을 때 모든 이벤트 즉시 발행")
        void shouldPublishAllEventsImmediatelyWhenNoTransaction() {
            // Given
            TestDomainEvent event1 = new TestDomainEvent("event-1");
            TestDomainEvent event2 = new TestDomainEvent("event-2");
            List<TestDomainEvent> events = List.of(event1, event2);

            try (MockedStatic<TransactionSynchronizationManager> mocked =
                    Mockito.mockStatic(TransactionSynchronizationManager.class)) {
                mocked.when(TransactionSynchronizationManager::isSynchronizationActive)
                        .thenReturn(false);

                // When
                registry.registerAllForPublish(events);

                // Then
                then(eventPublisher).should().publishEvent(event1);
                then(eventPublisher).should().publishEvent(event2);
            }
        }
    }

    @Nested
    @DisplayName("publish() 테스트")
    class Publish {

        @Test
        @DisplayName("[성공] Object 타입 이벤트 즉시 발행")
        void shouldPublishObjectEventImmediately() {
            // Given
            Object event = new Object();

            // When
            registry.publish(event);

            // Then
            then(eventPublisher).should().publishEvent(event);
        }

        @Test
        @DisplayName("[성공] DomainEvent 타입 이벤트 즉시 발행")
        void shouldPublishDomainEventImmediately() {
            // Given
            TestDomainEvent event = new TestDomainEvent("immediate-event");

            // When
            registry.publish(event);

            // Then
            then(eventPublisher).should().publishEvent(event);
        }
    }

    @Nested
    @DisplayName("registerObjectForPublish() 테스트")
    class RegisterObjectForPublish {

        @Test
        @DisplayName("[성공] 트랜잭션 컨텍스트 없을 때 즉시 발행")
        void shouldPublishImmediatelyWhenNoTransaction() {
            // Given
            Object event = new Object();

            try (MockedStatic<TransactionSynchronizationManager> mocked =
                    Mockito.mockStatic(TransactionSynchronizationManager.class)) {
                mocked.when(TransactionSynchronizationManager::isSynchronizationActive)
                        .thenReturn(false);

                // When
                registry.registerObjectForPublish(event);

                // Then
                then(eventPublisher).should().publishEvent(event);
            }
        }

        @Test
        @DisplayName("[성공] 트랜잭션 컨텍스트 있을 때 즉시 발행 안 함")
        void shouldNotPublishImmediatelyWhenTransactionActive() {
            // Given
            Object event = new Object();

            try (MockedStatic<TransactionSynchronizationManager> mocked =
                    Mockito.mockStatic(TransactionSynchronizationManager.class)) {
                mocked.when(TransactionSynchronizationManager::isSynchronizationActive)
                        .thenReturn(true);
                mocked.when(
                                () ->
                                        TransactionSynchronizationManager.registerSynchronization(
                                                Mockito.any(TransactionSynchronization.class)))
                        .then(invocation -> null);

                // When
                registry.registerObjectForPublish(event);

                // Then - 즉시 발행 없음
                then(eventPublisher).should(never()).publishEvent(event);
            }
        }
    }
}
