package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExecutionId VO 테스트
 *
 * TDD Phase: Red → Green
 * - Long 기반 auto-increment ID
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class ExecutionIdTest {

    @Test
    void shouldCreateNewIdWithNullValue() {
        // When
        ExecutionId id = ExecutionId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNull();
    }

    @Test
    void shouldReturnTrueForIsNewWhenValueIsNull() {
        // Given
        ExecutionId id = ExecutionId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForIsNewWhenValueIsNotNull() {
        // Given
        ExecutionId id = new ExecutionId(1L);

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateIdWithSpecificValue() {
        // When
        ExecutionId id = new ExecutionId(123L);

        // Then
        assertThat(id.value()).isEqualTo(123L);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    void shouldCreateIdFromExistingValue() {
        // Given
        Long value = 456L;

        // When
        ExecutionId id = new ExecutionId(value);

        // Then
        assertThat(id.value()).isEqualTo(value);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        // Given
        ExecutionId id1 = new ExecutionId(789L);
        ExecutionId id2 = new ExecutionId(789L);

        // When & Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
