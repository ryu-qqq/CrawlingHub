package com.ryuqq.crawlinghub.domain.schedule.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ScheduleCreatedEvent Domain Event 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@DisplayName("ScheduleCreatedEvent Domain Event 단위 테스트")
class ScheduleCreatedEventTest {

    @Nested
    @DisplayName("of 메서드는")
    class Describe_of {

        @Test
        @DisplayName("유효한 파라미터로 이벤트를 생성한다")
        void it_creates_event_with_valid_parameters() {
            // Given
            Long scheduleId = 1L;
            Long sellerId = 100L;
            String cronExpression = "0 * * * *";
            String outboxIdemKey = "seller:100:event:CREATE:abc123";

            // When
            ScheduleCreatedEvent event = ScheduleCreatedEvent.of(
                scheduleId,
                sellerId,
                cronExpression,
                outboxIdemKey
            );

            // Then
            assertThat(event).isNotNull();
            assertThat(event.scheduleId()).isEqualTo(scheduleId);
            assertThat(event.sellerId()).isEqualTo(sellerId);
            assertThat(event.cronExpression()).isEqualTo(cronExpression);
            assertThat(event.outboxIdemKey()).isEqualTo(outboxIdemKey);
            assertThat(event.occurredAt()).isNotNull();
            assertThat(event.occurredAt()).isBefore(Instant.now().plusSeconds(1));
        }

        @Test
        @DisplayName("발생 시각이 현재 시간 이후가 아니다")
        void it_sets_occurred_at_to_current_time() {
            // Given
            Instant beforeCreation = Instant.now();

            // When
            ScheduleCreatedEvent event = ScheduleCreatedEvent.of(
                1L,
                100L,
                "0 * * * *",
                "seller:100:event:CREATE:abc123"
            );

            // Then
            assertThat(event.occurredAt()).isAfter(beforeCreation.minusSeconds(1));
            assertThat(event.occurredAt()).isBefore(Instant.now().plusSeconds(1));
        }
    }

    @Nested
    @DisplayName("ScheduleEvent 인터페이스 구현")
    class Describe_schedule_event_interface {

        @Test
        @DisplayName("ScheduleEvent 인터페이스를 구현한다")
        void it_implements_schedule_event_interface() {
            // Given
            ScheduleCreatedEvent event = ScheduleCreatedEvent.of(
                1L,
                100L,
                "0 * * * *",
                "seller:100:event:CREATE:abc123"
            );

            // Then
            assertThat(event).isInstanceOf(ScheduleEvent.class);
            assertThat(event.scheduleId()).isEqualTo(1L);
            assertThat(event.sellerId()).isEqualTo(100L);
            assertThat(event.occurredAt()).isNotNull();
        }
    }
}

