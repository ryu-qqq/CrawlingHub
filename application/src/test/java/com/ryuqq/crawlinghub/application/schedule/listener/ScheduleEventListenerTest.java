package com.ryuqq.crawlinghub.application.schedule.listener;

import com.ryuqq.crawlinghub.application.schedule.orchestrator.ScheduleOutboxProcessor;
import com.ryuqq.crawlinghub.application.schedule.port.out.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleCreatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ScheduleEventListener 단위 테스트
 *
 * <p>트랜잭션 커밋 후 이벤트를 수신하여 비동기로 Outbox Processor를 호출하는 로직을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleEventListener 단위 테스트")
class ScheduleEventListenerTest {

    @Mock
    private SellerCrawlScheduleOutboxPort outboxPort;

    @Mock
    private ScheduleOutboxProcessor outboxProcessor;

    @InjectMocks
    private ScheduleEventListener sut;

    @Nested
    @DisplayName("handleScheduleCreated 메서드는")
    class Describe_handle_schedule_created {

        private ScheduleCreatedEvent event;
        private SellerCrawlScheduleOutbox outbox;

        @BeforeEach
        void setUp() {
            event = ScheduleCreatedEvent.of(
                1L,
                100L,
                "0 * * * *",
                "seller:100:event:CREATE:abc123"
            );
        }

        @Nested
        @DisplayName("PENDING 상태의 Outbox가 존재하면")
        class Context_with_pending_outbox {

            @BeforeEach
            void setUp() {
                outbox = SellerCrawlScheduleOutbox.forEventBridgeRegistration(
                    100L,
                    "{\"scheduleId\":1,\"sellerId\":100,\"cronExpression\":\"0 * * * *\"}",
                    "seller:100:event:CREATE:abc123"
                );

                given(outboxPort.findByIdemKey(event.outboxIdemKey()))
                    .willReturn(Optional.of(outbox));
            }

            @Test
            @DisplayName("Outbox Processor를 호출한다")
            void it_calls_outbox_processor() {
                // When
                sut.handleScheduleCreated(event);

                // Then
                then(outboxProcessor).should().processOne(outbox);
            }

            @Test
            @DisplayName("예외가 발생하지 않는다")
            void it_does_not_throw_exception() {
                // When & Then
                assertThatCode(() -> sut.handleScheduleCreated(event))
                    .doesNotThrowAnyException();
            }
        }

        @Nested
        @DisplayName("Outbox가 존재하지 않으면")
        class Context_with_no_outbox {

            @BeforeEach
            void setUp() {
                given(outboxPort.findByIdemKey(event.outboxIdemKey()))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("Outbox Processor를 호출하지 않는다")
            void it_does_not_call_outbox_processor() {
                // When
                sut.handleScheduleCreated(event);

                // Then
                then(outboxProcessor).should(never()).processOne(any());
            }

            @Test
            @DisplayName("예외가 발생하지 않는다")
            void it_does_not_throw_exception() {
                // When & Then
                assertThatCode(() -> sut.handleScheduleCreated(event))
                    .doesNotThrowAnyException();
            }
        }

        @Nested
        @DisplayName("이미 처리된 Outbox면")
        class Context_with_already_processed_outbox {

            @BeforeEach
            void setUp() {
                outbox = SellerCrawlScheduleOutbox.forEventBridgeRegistration(
                    100L,
                    "{\"scheduleId\":1,\"sellerId\":100,\"cronExpression\":\"0 * * * *\"}",
                    "seller:100:event:CREATE:abc123"
                );
                // markCompleted()를 호출하면 WriteAheadState가 COMPLETED로 변경됨
                outbox.markCompleted();

                given(outboxPort.findByIdemKey(event.outboxIdemKey()))
                    .willReturn(Optional.of(outbox));
            }

            @Test
            @DisplayName("Outbox Processor를 호출하지 않는다")
            void it_does_not_call_outbox_processor() {
                // When
                sut.handleScheduleCreated(event);

                // Then
                then(outboxProcessor).should(never()).processOne(any());
            }

            @Test
            @DisplayName("WriteAheadState가 COMPLETED인지 확인")
            void it_checks_wal_state_is_completed() {
                // Then
                assertThat(outbox.getWalState())
                    .isEqualTo(SellerCrawlScheduleOutbox.WriteAheadState.COMPLETED);
            }
        }

        @Nested
        @DisplayName("Outbox Processor 호출 시 예외가 발생하면")
        class Context_when_outbox_processor_throws_exception {

            @BeforeEach
            void setUp() {
                outbox = SellerCrawlScheduleOutbox.forEventBridgeRegistration(
                    100L,
                    "{\"scheduleId\":1,\"sellerId\":100,\"cronExpression\":\"0 * * * *\"}",
                    "seller:100:event:CREATE:abc123"
                );

                given(outboxPort.findByIdemKey(event.outboxIdemKey()))
                    .willReturn(Optional.of(outbox));

                // Outbox Processor 호출 시 예외 발생
                org.mockito.BDDMockito.willThrow(new RuntimeException("Processor error"))
                    .given(outboxProcessor).processOne(any());
            }

            @Test
            @DisplayName("예외를 전파하지 않고 무시한다")
            void it_does_not_propagate_exception() {
                // When & Then
                assertThatCode(() -> sut.handleScheduleCreated(event))
                    .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("handleScheduleUpdated 메서드는")
    class Describe_handle_schedule_updated {

        private ScheduleUpdatedEvent event;
        private SellerCrawlScheduleOutbox outbox;

        @BeforeEach
        void setUp() {
            event = ScheduleUpdatedEvent.of(
                1L,
                100L,
                "0 0 * * *",
                "seller:100:event:UPDATE_1:def456"
            );
        }

        @Nested
        @DisplayName("PENDING 상태의 Outbox가 존재하면")
        class Context_with_pending_outbox {

            @BeforeEach
            void setUp() {
                outbox = SellerCrawlScheduleOutbox.forEventBridgeUpdate(
                    100L,
                    "{\"scheduleId\":1,\"sellerId\":100,\"cronExpression\":\"0 0 * * *\"}",
                    "seller:100:event:UPDATE_1:def456"
                );

                given(outboxPort.findByIdemKey(event.outboxIdemKey()))
                    .willReturn(Optional.of(outbox));
            }

            @Test
            @DisplayName("Outbox Processor를 호출한다")
            void it_calls_outbox_processor() {
                // When
                sut.handleScheduleUpdated(event);

                // Then
                then(outboxProcessor).should().processOne(outbox);
            }

            @Test
            @DisplayName("예외가 발생하지 않는다")
            void it_does_not_throw_exception() {
                // When & Then
                assertThatCode(() -> sut.handleScheduleUpdated(event))
                    .doesNotThrowAnyException();
            }
        }

        @Nested
        @DisplayName("Outbox가 존재하지 않으면")
        class Context_with_no_outbox {

            @BeforeEach
            void setUp() {
                given(outboxPort.findByIdemKey(event.outboxIdemKey()))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("Outbox Processor를 호출하지 않는다")
            void it_does_not_call_outbox_processor() {
                // When
                sut.handleScheduleUpdated(event);

                // Then
                then(outboxProcessor).should(never()).processOne(any());
            }
        }
    }
}

