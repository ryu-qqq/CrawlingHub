package com.ryuqq.crawlinghub.domain.schedule.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for ScheduleUpdatedEvent domain event.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
class ScheduleUpdatedEventTest {

    @Test
    @DisplayName("유효한 값으로 ScheduleUpdatedEvent 생성 성공")
    void createEventWithValidValues() {
        // given
        Long scheduleId = 1L;
        String ruleName = "test-rule";
        String cronExpression = "0 0 * * *";
        String description = "Updated schedule description";

        // when
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(
                scheduleId, ruleName, cronExpression, description
        );

        // then
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isEqualTo(ruleName);
        assertThat(event.cronExpression()).isEqualTo(cronExpression);
        assertThat(event.description()).isEqualTo(description);
    }

    @Test
    @DisplayName("null scheduleId로 생성 시 예외 발생")
    void createEventWithNullScheduleId() {
        assertThatThrownBy(() -> new ScheduleUpdatedEvent(
                null, "rule", "0 0 * * *", "description"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule ID cannot be null");
    }

    @Test
    @DisplayName("null ruleName으로 생성 시 예외 발생")
    void createEventWithNullRuleName() {
        assertThatThrownBy(() -> new ScheduleUpdatedEvent(
                1L, null, "0 0 * * *", "description"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    @DisplayName("빈 ruleName으로 생성 시 예외 발생")
    void createEventWithBlankRuleName() {
        assertThatThrownBy(() -> new ScheduleUpdatedEvent(
                1L, "  ", "0 0 * * *", "description"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    @DisplayName("null cronExpression으로 생성 시 예외 발생")
    void createEventWithNullCronExpression() {
        assertThatThrownBy(() -> new ScheduleUpdatedEvent(
                1L, "rule", null, "description"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron expression cannot be null or blank");
    }

    @Test
    @DisplayName("null description으로 생성 시 예외 발생")
    void createEventWithNullDescription() {
        assertThatThrownBy(() -> new ScheduleUpdatedEvent(
                1L, "rule", "0 0 * * *", null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description cannot be null or blank");
    }
}
