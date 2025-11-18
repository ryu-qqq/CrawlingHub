package com.ryuqq.crawlinghub.domain.eventbridge.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.crawlinghub.domain.fixture.eventbridge.SchedulerStatusFixture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SchedulerStatus Enum 테스트")
class SchedulerStatusTest {

    @Test
    @DisplayName("PENDING, ACTIVE, INACTIVE 값을 제공해야 한다")
    void shouldHaveCorrectValues() {
        assertAll(
            () -> assertNotNull(SchedulerStatusFixture.pending()),
            () -> assertNotNull(SchedulerStatusFixture.active()),
            () -> assertNotNull(SchedulerStatusFixture.inactive()),
            () -> assertEquals("PENDING", SchedulerStatusFixture.pending().name()),
            () -> assertEquals("ACTIVE", SchedulerStatusFixture.active().name()),
            () -> assertEquals("INACTIVE", SchedulerStatusFixture.inactive().name())
        );
    }

    @Test
    @DisplayName("PENDING 상태는 ACTIVE로만 전환할 수 있다")
    void pendingShouldTransitionOnlyToActive() {
        SchedulerStatus pending = SchedulerStatusFixture.pending();

        assertAll(
            () -> assertTrue(pending.canTransitionTo(SchedulerStatusFixture.active())),
            () -> assertFalse(pending.canTransitionTo(SchedulerStatusFixture.inactive())),
            () -> assertFalse(pending.canTransitionTo(SchedulerStatusFixture.pending()))
        );
    }

    @Test
    @DisplayName("ACTIVE 상태는 INACTIVE로만 전환할 수 있다")
    void activeShouldTransitionOnlyToInactive() {
        SchedulerStatus active = SchedulerStatusFixture.active();

        assertAll(
            () -> assertTrue(active.canTransitionTo(SchedulerStatusFixture.inactive())),
            () -> assertFalse(active.canTransitionTo(SchedulerStatusFixture.pending())),
            () -> assertFalse(active.canTransitionTo(SchedulerStatusFixture.active()))
        );
    }

    @Test
    @DisplayName("INACTIVE 상태는 ACTIVE로만 전환할 수 있다")
    void inactiveShouldTransitionOnlyToActive() {
        SchedulerStatus inactive = SchedulerStatusFixture.inactive();

        assertAll(
            () -> assertTrue(inactive.canTransitionTo(SchedulerStatusFixture.active())),
            () -> assertFalse(inactive.canTransitionTo(SchedulerStatusFixture.pending())),
            () -> assertFalse(inactive.canTransitionTo(SchedulerStatusFixture.inactive()))
        );
    }
}

