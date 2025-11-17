package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExecutionId VO 테스트
 *
 * TDD Phase: Red → Green
 * - UUID 고유성 검증
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class ExecutionIdTest {

    @Test
    void shouldGenerateUniqueExecutionId() {
        ExecutionId id1 = ExecutionId.generate();
        ExecutionId id2 = ExecutionId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldCreateIdUsingForNew() {
        // When
        ExecutionId id = ExecutionId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldReturnTrueForIsNew() {
        // Given
        ExecutionId id = ExecutionId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldCreateIdFromExistingValue() {
        // Given
        ExecutionId originalId = ExecutionId.generate();

        // When
        ExecutionId reconstructedId = new ExecutionId(originalId.value());

        // Then
        assertThat(reconstructedId).isEqualTo(originalId);
        assertThat(reconstructedId.value()).isEqualTo(originalId.value());
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        // Given
        ExecutionId id1 = ExecutionId.generate();
        ExecutionId id2 = new ExecutionId(id1.value());

        // When & Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
