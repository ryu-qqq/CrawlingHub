package com.ryuqq.crawlinghub.application.schedule.listener;

import com.ryuqq.crawlinghub.application.schedule.manager.ScheduleOutboxStateManager;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleCreatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * ScheduleEventListener 단위 테스트
 *
 * <p>트랜잭션 커밋 후 Schedule Event를 수신하여 비동기로 Outbox를 즉시 처리하는 로직을 검증합니다.</p>
 *
 * <p><strong>변경 사항 (2025-11-10):</strong></p>
 * <ul>
 *   <li>✅ ScheduleOutboxProcessor 제거 → ScheduleOutboxStateManager 사용</li>
 *   <li>✅ handleScheduleCreated/Updated 통합 → handleScheduleEvent</li>
 *   <li>✅ SellerCrawlScheduleOutbox → ScheduleOutbox</li>
 *   <li>✅ StateManager.processOne() 직접 호출</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleEventListener 단위 테스트")
class ScheduleEventListenerTest {

    @Mock
    private ScheduleOutboxStateManager stateManager;

    @InjectMocks
    private ScheduleEventListener sut;

    @Nested
    @DisplayName("handleScheduleEvent 메서드는")
    class Describe_handle_schedule_event {

        @Nested
        @DisplayName("ScheduleCreatedEvent를 수신하면")
        class Context_with_schedule_created_event {

            private ScheduleCreatedEvent event;
            private ScheduleOutbox outbox;

            @BeforeEach
            void setUp() {
                event = ScheduleCreatedEvent.of(
                    1L,
                    100L,
                    "0 * * * *",
                    "seller:100:event:CREATE:abc123"
                );

                outbox = ScheduleOutbox.forEventBridgeRegistration(
                    100L,
                    "{\"scheduleId\":1,\"sellerId\":100,\"cronExpression\":\"0 * * * *\"}",
                    "seller:100:event:CREATE:abc123"
                );
            }

            @Nested
            @DisplayName("PENDING 상태의 Outbox가 존재하면")
            class Context_with_pending_outbox {

                @BeforeEach
                void setUp() {
                    given(stateManager.findByIdemKey(event.outboxIdemKey()))
                        .willReturn(outbox);
                }

                @Test
                @DisplayName("StateManager.processOne()을 호출한다")
                void it_calls_state_manager_process_one() {
                    // When
                    sut.handleScheduleEvent(event);

                    // Then
                    then(stateManager).should().findByIdemKey(event.outboxIdemKey());
                    then(stateManager).should().processOne(outbox);
                }

                @Test
                @DisplayName("예외가 발생하지 않는다")
                void it_does_not_throw_exception() {
                    // When & Then
                    assertThatCode(() -> sut.handleScheduleEvent(event))
                        .doesNotThrowAnyException();
                }
            }

            @Nested
            @DisplayName("Outbox가 존재하지 않으면")
            class Context_with_no_outbox {

                @BeforeEach
                void setUp() {
                    given(stateManager.findByIdemKey(event.outboxIdemKey()))
                        .willReturn(null);
                }

                @Test
                @DisplayName("StateManager.processOne()을 호출하지 않는다")
                void it_does_not_call_state_manager_process_one() {
                    // When
                    sut.handleScheduleEvent(event);

                    // Then
                    then(stateManager).should().findByIdemKey(event.outboxIdemKey());
                    then(stateManager).should(never()).processOne(any());
                }

                @Test
                @DisplayName("예외가 발생하지 않는다")
                void it_does_not_throw_exception() {
                    // When & Then
                    assertThatCode(() -> sut.handleScheduleEvent(event))
                        .doesNotThrowAnyException();
                }
            }

            @Nested
            @DisplayName("이미 처리된 Outbox면")
            class Context_with_already_processed_outbox {

                @BeforeEach
                void setUp() {
                    // markCompleted()를 호출하면 WriteAheadState가 COMPLETED로 변경됨
                    outbox.markCompleted();

                    given(stateManager.findByIdemKey(event.outboxIdemKey()))
                        .willReturn(outbox);
                }

                @Test
                @DisplayName("StateManager.processOne()을 호출하지 않는다")
                void it_does_not_call_state_manager_process_one() {
                    // When
                    sut.handleScheduleEvent(event);

                    // Then
                    then(stateManager).should().findByIdemKey(event.outboxIdemKey());
                    then(stateManager).should(never()).processOne(any());
                }
            }

            @Nested
            @DisplayName("StateManager.processOne() 호출 시 예외가 발생하면")
            class Context_when_state_manager_throws_exception {

                @BeforeEach
                void setUp() {
                    given(stateManager.findByIdemKey(event.outboxIdemKey()))
                        .willReturn(outbox);

                    // StateManager.processOne() 호출 시 예외 발생
                    org.mockito.BDDMockito.willThrow(new RuntimeException("StateManager error"))
                        .given(stateManager).processOne(any());
                }

                @Test
                @DisplayName("예외를 전파하지 않고 무시한다 (Fallback: @Scheduled)")
                void it_does_not_propagate_exception() {
                    // When & Then
                    assertThatCode(() -> sut.handleScheduleEvent(event))
                        .doesNotThrowAnyException();
                }
            }
        }

        @Nested
        @DisplayName("ScheduleUpdatedEvent를 수신하면")
        class Context_with_schedule_updated_event {

            private ScheduleUpdatedEvent event;
            private ScheduleOutbox outbox;

            @BeforeEach
            void setUp() {
                event = ScheduleUpdatedEvent.of(
                    1L,
                    100L,
                    "0 0 * * *",
                    "seller:100:event:UPDATE_1:def456"
                );

                outbox = ScheduleOutbox.forEventBridgeUpdate(
                    100L,
                    "{\"scheduleId\":1,\"sellerId\":100,\"cronExpression\":\"0 0 * * *\"}",
                    "seller:100:event:UPDATE_1:def456"
                );
            }

            @Nested
            @DisplayName("PENDING 상태의 Outbox가 존재하면")
            class Context_with_pending_outbox {

                @BeforeEach
                void setUp() {
                    given(stateManager.findByIdemKey(event.outboxIdemKey()))
                        .willReturn(outbox);
                }

                @Test
                @DisplayName("StateManager.processOne()을 호출한다")
                void it_calls_state_manager_process_one() {
                    // When
                    sut.handleScheduleEvent(event);

                    // Then
                    then(stateManager).should().findByIdemKey(event.outboxIdemKey());
                    then(stateManager).should().processOne(outbox);
                }

                @Test
                @DisplayName("예외가 발생하지 않는다")
                void it_does_not_throw_exception() {
                    // When & Then
                    assertThatCode(() -> sut.handleScheduleEvent(event))
                        .doesNotThrowAnyException();
                }
            }

            @Nested
            @DisplayName("Outbox가 존재하지 않으면")
            class Context_with_no_outbox {

                @BeforeEach
                void setUp() {
                    given(stateManager.findByIdemKey(event.outboxIdemKey()))
                        .willReturn(null);
                }

                @Test
                @DisplayName("StateManager.processOne()을 호출하지 않는다")
                void it_does_not_call_state_manager_process_one() {
                    // When
                    sut.handleScheduleEvent(event);

                    // Then
                    then(stateManager).should().findByIdemKey(event.outboxIdemKey());
                    then(stateManager).should(never()).processOne(any());
                }
            }
        }
    }
}
