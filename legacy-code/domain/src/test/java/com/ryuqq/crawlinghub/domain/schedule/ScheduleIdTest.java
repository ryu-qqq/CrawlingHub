package com.ryuqq.crawlinghub.domain.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for ScheduleId value object.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
class ScheduleIdTest {

    @Test
    @DisplayName("Valid ID로 ScheduleId 생성 성공")
    void createScheduleIdWithValidId() {
        // given
        Long validId = 1L;

        // when
        ScheduleId scheduleId = ScheduleId.of(validId);

        // then
        assertThat(scheduleId.value()).isEqualTo(validId);
    }

    @Test
    @DisplayName("null ID로 ScheduleId 생성 시 예외 발생")
    void createScheduleIdWithNullId() {
        // given
        Long nullId = null;

        // when & then
        assertThatThrownBy(() -> ScheduleId.of(nullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule ID must be positive");
    }

    @Test
    @DisplayName("0으로 ScheduleId 생성 시 예외 발생")
    void createScheduleIdWithZeroId() {
        // given
        Long zeroId = 0L;

        // when & then
        assertThatThrownBy(() -> ScheduleId.of(zeroId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule ID must be positive");
    }

    @Test
    @DisplayName("음수 ID로 ScheduleId 생성 시 예외 발생")
    void createScheduleIdWithNegativeId() {
        // given
        Long negativeId = -1L;

        // when & then
        assertThatThrownBy(() -> ScheduleId.of(negativeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule ID must be positive");
    }

    @Test
    @DisplayName("동일한 값의 ScheduleId는 equals 성공")
    void scheduleIdEquality() {
        // given
        Long id = 1L;
        ScheduleId scheduleId1 = ScheduleId.of(id);
        ScheduleId scheduleId2 = ScheduleId.of(id);

        // when & then
        assertThat(scheduleId1).isEqualTo(scheduleId2);
        assertThat(scheduleId1.hashCode()).isEqualTo(scheduleId2.hashCode());
    }
}
