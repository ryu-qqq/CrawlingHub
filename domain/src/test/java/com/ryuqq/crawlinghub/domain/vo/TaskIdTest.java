package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TaskId Value Object 테스트
 *
 * TDD Phase: Red
 * - UUID 기반 고유 ID 생성 검증
 */
class TaskIdTest {

    @Test
    void shouldGenerateUniqueTaskId() {
        TaskId taskId1 = TaskId.generate();
        TaskId taskId2 = TaskId.generate();
        assertThat(taskId1).isNotEqualTo(taskId2);
    }
}
