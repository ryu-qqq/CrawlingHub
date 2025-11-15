package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OutboxId VO 테스트
 *
 * TDD Phase: Red
 * - UUID 고유성 검증
 */
class OutboxIdTest {

    @Test
    void shouldGenerateUniqueOutboxId() {
        OutboxId id1 = OutboxId.generate();
        OutboxId id2 = OutboxId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }
}
