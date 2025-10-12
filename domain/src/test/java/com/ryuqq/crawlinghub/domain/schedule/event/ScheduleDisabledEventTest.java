package com.ryuqq.crawlinghub.domain.schedule.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for ScheduleDisabledEvent domain event.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
class ScheduleDisabledEventTest {

    @Test
    @DisplayName("유효한 값으로 ScheduleDisabledEvent 생성 성공")
    void createEventWithValidValues() {
        // given
        Long scheduleId = 1L;
        String ruleName = "test-rule";

        // when
        ScheduleDisabledEvent event = new ScheduleDisabledEvent(scheduleId, ruleName);

        // then
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isEqualTo(ruleName);
    }

    @Test
    @DisplayName("null scheduleId로 생성 시 예외 발생")
    void createEventWithNullScheduleId() {
        assertThatThrownBy(() -> new ScheduleDisabledEvent(null, "rule"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule ID cannot be null");
    }

    @Test
    @DisplayName("null ruleName으로 생성 시 예외 발생")
    void createEventWithNullRuleName() {
        assertThatThrownBy(() -> new ScheduleDisabledEvent(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    @DisplayName("빈 ruleName으로 생성 시 예외 발생")
    void createEventWithBlankRuleName() {
        assertThatThrownBy(() -> new ScheduleDisabledEvent(1L, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }
}
