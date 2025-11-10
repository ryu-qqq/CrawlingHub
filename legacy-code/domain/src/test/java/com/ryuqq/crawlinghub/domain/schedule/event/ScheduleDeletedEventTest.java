package com.ryuqq.crawlinghub.domain.schedule.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for ScheduleDeletedEvent domain event.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
class ScheduleDeletedEventTest {

    @Test
    @DisplayName("유효한 값으로 ScheduleDeletedEvent 생성 성공 (enabled=true)")
    void createEventWithValidValuesEnabled() {
        // given
        Long scheduleId = 1L;
        String ruleName = "test-rule";
        boolean wasEnabled = true;

        // when
        ScheduleDeletedEvent event = new ScheduleDeletedEvent(scheduleId, ruleName, wasEnabled);

        // then
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isEqualTo(ruleName);
        assertThat(event.wasEnabled()).isTrue();
    }

    @Test
    @DisplayName("유효한 값으로 ScheduleDeletedEvent 생성 성공 (enabled=false)")
    void createEventWithValidValuesDisabled() {
        // given
        Long scheduleId = 1L;
        String ruleName = "test-rule";
        boolean wasEnabled = false;

        // when
        ScheduleDeletedEvent event = new ScheduleDeletedEvent(scheduleId, ruleName, wasEnabled);

        // then
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isEqualTo(ruleName);
        assertThat(event.wasEnabled()).isFalse();
    }

    @Test
    @DisplayName("null scheduleId로 생성 시 예외 발생")
    void createEventWithNullScheduleId() {
        assertThatThrownBy(() -> new ScheduleDeletedEvent(null, "rule", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule ID cannot be null");
    }

    @Test
    @DisplayName("null ruleName으로 생성 시 예외 발생")
    void createEventWithNullRuleName() {
        assertThatThrownBy(() -> new ScheduleDeletedEvent(1L, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    @DisplayName("빈 ruleName으로 생성 시 예외 발생")
    void createEventWithBlankRuleName() {
        assertThatThrownBy(() -> new ScheduleDeletedEvent(1L, "  ", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }
}
