package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OutboxId VO 테스트
 *
 * TDD Phase: Red → Green
 * - UUID 고유성 검증
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class OutboxIdTest {

    @Test
    void shouldGenerateUniqueOutboxId() {
        OutboxId id1 = OutboxId.generate();
        OutboxId id2 = OutboxId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldCreateIdUsingForNew() {
        // When
        OutboxId id = OutboxId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldReturnTrueForIsNew() {
        // Given
        OutboxId id = OutboxId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }
}
