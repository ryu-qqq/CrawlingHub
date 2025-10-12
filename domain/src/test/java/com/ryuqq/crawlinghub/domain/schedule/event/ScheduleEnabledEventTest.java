package com.ryuqq.crawlinghub.domain.schedule.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for ScheduleEnabledEvent domain event.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
class ScheduleEnabledEventTest {

    @Test
    @DisplayName("유효한 값으로 ScheduleEnabledEvent 생성 성공")
    void createEventWithValidValues() {
        // given
        Long scheduleId = 1L;
        String ruleName = "test-rule";
        String cronExpression = "0 0 * * *";
        String scheduleName = "Test Schedule";
        String targetInput = "{\"scheduleId\":1}";

        // when
        ScheduleEnabledEvent event = new ScheduleEnabledEvent(
                scheduleId, ruleName, cronExpression, scheduleName, targetInput
        );

        // then
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isEqualTo(ruleName);
        assertThat(event.cronExpression()).isEqualTo(cronExpression);
        assertThat(event.scheduleName()).isEqualTo(scheduleName);
        assertThat(event.targetInput()).isEqualTo(targetInput);
    }

    @Test
    @DisplayName("null scheduleId로 생성 시 예외 발생")
    void createEventWithNullScheduleId() {
        assertThatThrownBy(() -> new ScheduleEnabledEvent(
                null, "rule", "0 0 * * *", "name", "{}"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule ID cannot be null");
    }

    @Test
    @DisplayName("null ruleName으로 생성 시 예외 발생")
    void createEventWithNullRuleName() {
        assertThatThrownBy(() -> new ScheduleEnabledEvent(
                1L, null, "0 0 * * *", "name", "{}"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    @DisplayName("빈 ruleName으로 생성 시 예외 발생")
    void createEventWithBlankRuleName() {
        assertThatThrownBy(() -> new ScheduleEnabledEvent(
                1L, "  ", "0 0 * * *", "name", "{}"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    @DisplayName("null cronExpression으로 생성 시 예외 발생")
    void createEventWithNullCronExpression() {
        assertThatThrownBy(() -> new ScheduleEnabledEvent(
                1L, "rule", null, "name", "{}"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron expression cannot be null or blank");
    }

    @Test
    @DisplayName("null scheduleName으로 생성 시 예외 발생")
    void createEventWithNullScheduleName() {
        assertThatThrownBy(() -> new ScheduleEnabledEvent(
                1L, "rule", "0 0 * * *", null, "{}"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule name cannot be null or blank");
    }

    @Test
    @DisplayName("null targetInput으로 생성 시 예외 발생")
    void createEventWithNullTargetInput() {
        assertThatThrownBy(() -> new ScheduleEnabledEvent(
                1L, "rule", "0 0 * * *", "name", null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target input cannot be null or blank");
    }
}
