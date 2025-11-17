package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxEventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SchedulerOutboxEventType VO 테스트
 */
class SchedulerOutboxEventTypeTest {

    @Test
    void shouldHaveAllEventTypes() {
        // When
        SchedulerOutboxEventType[] types = SchedulerOutboxEventType.values();

        // Then
        assertThat(types).hasSize(3);
        assertThat(types).contains(
                SchedulerOutboxEventType.SCHEDULE_REGISTERED,
                SchedulerOutboxEventType.SCHEDULE_UPDATED,
                SchedulerOutboxEventType.SCHEDULE_DEACTIVATED
        );
    }

    @Test
    void shouldCreateFromStringUsingOf() {
        // When
        SchedulerOutboxEventType registered = SchedulerOutboxEventType.of("SCHEDULE_REGISTERED");
        SchedulerOutboxEventType updated = SchedulerOutboxEventType.of("schedule_updated");
        SchedulerOutboxEventType deactivated = SchedulerOutboxEventType.of("Schedule_Deactivated");

        // Then
        assertThat(registered).isEqualTo(SchedulerOutboxEventType.SCHEDULE_REGISTERED);
        assertThat(updated).isEqualTo(SchedulerOutboxEventType.SCHEDULE_UPDATED);
        assertThat(deactivated).isEqualTo(SchedulerOutboxEventType.SCHEDULE_DEACTIVATED);
    }

    @Test
    void shouldThrowExceptionWhenNullValue() {
        // When & Then
        assertThatThrownBy(() -> SchedulerOutboxEventType.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SchedulerOutboxEventType cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenInvalidValue() {
        // When & Then
        assertThatThrownBy(() -> SchedulerOutboxEventType.of("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
