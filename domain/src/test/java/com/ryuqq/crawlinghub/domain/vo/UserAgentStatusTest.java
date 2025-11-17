package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserAgentStatus Enum 테스트
 *
 * TDD Phase: Red → Green
 * - 3가지 상태 존재 검증 (ACTIVE, SUSPENDED, BLOCKED)
 * - of() 정적 팩토리 메서드 검증 (String → Enum 변환)
 */
class UserAgentStatusTest {

    @Test
    void shouldHaveThreeStatuses() {
        assertThat(UserAgentStatus.values()).containsExactly(
            UserAgentStatus.ACTIVE,
            UserAgentStatus.SUSPENDED,
            UserAgentStatus.BLOCKED
        );
    }

    @Test
    void shouldCreateFromValidString() {
        // Given
        String value = "ACTIVE";

        // When
        UserAgentStatus result = UserAgentStatus.of(value);

        // Then
        assertThat(result).isEqualTo(UserAgentStatus.ACTIVE);
    }

    @Test
    void shouldCreateFromLowercaseString() {
        // Given
        String value = "suspended";

        // When
        UserAgentStatus result = UserAgentStatus.of(value);

        // Then
        assertThat(result).isEqualTo(UserAgentStatus.SUSPENDED);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        assertThatThrownBy(() -> UserAgentStatus.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserAgentStatus cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenValueIsInvalid() {
        // Given
        String invalidValue = "INVALID_STATUS";

        // When & Then
        assertThatThrownBy(() -> UserAgentStatus.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
