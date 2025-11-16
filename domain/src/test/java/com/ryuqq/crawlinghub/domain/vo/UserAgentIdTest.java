package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAgentId VO 테스트
 *
 * TDD Phase: Red → Green
 * - UUID 고유성 검증
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class UserAgentIdTest {

    @Test
    void shouldGenerateUniqueUserAgentId() {
        UserAgentId id1 = UserAgentId.generate();
        UserAgentId id2 = UserAgentId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldCreateIdUsingForNew() {
        // When
        UserAgentId id = UserAgentId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldReturnTrueForIsNew() {
        // Given
        UserAgentId id = UserAgentId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }
}
