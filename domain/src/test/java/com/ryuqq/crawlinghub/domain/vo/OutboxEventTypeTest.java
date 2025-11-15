package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OutboxEventType Enum 테스트
 *
 * TDD Phase: Red
 * - 2가지 이벤트 타입 존재 검증 (PRODUCT_CREATED, PRODUCT_UPDATED)
 */
class OutboxEventTypeTest {

    @Test
    void shouldHaveTwoEventTypes() {
        assertThat(OutboxEventType.values()).containsExactly(
            OutboxEventType.PRODUCT_CREATED,
            OutboxEventType.PRODUCT_UPDATED
        );
    }
}
