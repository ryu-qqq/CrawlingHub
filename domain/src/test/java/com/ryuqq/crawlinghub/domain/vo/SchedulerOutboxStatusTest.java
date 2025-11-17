package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SchedulerOutboxStatus VO 테스트
 */
class SchedulerOutboxStatusTest {

    @Test
    void shouldHaveAllStatuses() {
        // When
        SchedulerOutboxStatus[] statuses = SchedulerOutboxStatus.values();

        // Then
        assertThat(statuses).hasSize(4);
        assertThat(statuses).contains(
                SchedulerOutboxStatus.WAITING,
                SchedulerOutboxStatus.SENDING,
                SchedulerOutboxStatus.COMPLETED,
                SchedulerOutboxStatus.FAILED
        );
    }

    @Test
    void shouldCreateFromStringUsingOf() {
        // When
        SchedulerOutboxStatus waiting = SchedulerOutboxStatus.of("WAITING");
        SchedulerOutboxStatus sending = SchedulerOutboxStatus.of("sending");
        SchedulerOutboxStatus completed = SchedulerOutboxStatus.of("Completed");
        SchedulerOutboxStatus failed = SchedulerOutboxStatus.of("Failed");

        // Then
        assertThat(waiting).isEqualTo(SchedulerOutboxStatus.WAITING);
        assertThat(sending).isEqualTo(SchedulerOutboxStatus.SENDING);
        assertThat(completed).isEqualTo(SchedulerOutboxStatus.COMPLETED);
        assertThat(failed).isEqualTo(SchedulerOutboxStatus.FAILED);
    }

    @Test
    void shouldThrowExceptionWhenNullValue() {
        // When & Then
        assertThatThrownBy(() -> SchedulerOutboxStatus.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SchedulerOutboxStatus cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenInvalidValue() {
        // When & Then
        assertThatThrownBy(() -> SchedulerOutboxStatus.of("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
