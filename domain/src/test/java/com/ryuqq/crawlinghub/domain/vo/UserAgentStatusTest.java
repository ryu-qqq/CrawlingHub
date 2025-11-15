package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAgentStatus Enum 테스트
 *
 * TDD Phase: Red
 * - 3가지 상태 존재 검증 (ACTIVE, SUSPENDED, BLOCKED)
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
}
