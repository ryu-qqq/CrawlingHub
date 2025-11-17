package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAgentId VO 테스트
 *
 * TDD Phase: Red → Green
 * - Long 기반 auto-increment ID
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class UserAgentIdTest {

    @Test
    void shouldCreateNewIdWithNullValue() {
        // When
        UserAgentId id = UserAgentId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNull();
    }

    @Test
    void shouldReturnTrueForIsNewWhenValueIsNull() {
        // Given
        UserAgentId id = UserAgentId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForIsNewWhenValueIsNotNull() {
        // Given
        UserAgentId id = new UserAgentId(1L);

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateIdWithSpecificValue() {
        // When
        UserAgentId id = new UserAgentId(123L);

        // Then
        assertThat(id.value()).isEqualTo(123L);
        assertThat(id.isNew()).isFalse();
    }
}
