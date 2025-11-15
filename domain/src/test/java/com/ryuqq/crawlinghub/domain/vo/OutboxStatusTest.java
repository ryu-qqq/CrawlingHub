package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OutboxStatus Enum 테스트
 *
 * TDD Phase: Red
 * - 4가지 상태 존재 검증 (WAITING, SENDING, COMPLETED, FAILED)
 */
class OutboxStatusTest {

    @Test
    void shouldHaveFourStatuses() {
        assertThat(OutboxStatus.values()).containsExactly(
            OutboxStatus.WAITING,
            OutboxStatus.SENDING,
            OutboxStatus.COMPLETED,
            OutboxStatus.FAILED
        );
    }
}
