package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TaskId Value Object 테스트
 *
 * TDD Phase: Red → Green
 * - UUID 기반 고유 ID 생성 검증
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class TaskIdTest {

    @Test
    void shouldGenerateUniqueTaskId() {
        TaskId taskId1 = TaskId.generate();
        TaskId taskId2 = TaskId.generate();
        assertThat(taskId1).isNotEqualTo(taskId2);
    }

    @Test
    void shouldCreateIdUsingForNew() {
        // When
        TaskId id = TaskId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldReturnTrueForIsNew() {
        // Given
        TaskId id = TaskId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }
}
