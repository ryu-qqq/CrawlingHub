package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAgentId VO 테스트
 *
 * TDD Phase: Red
 * - UUID 고유성 검증
 */
class UserAgentIdTest {

    @Test
    void shouldGenerateUniqueUserAgentId() {
        UserAgentId id1 = UserAgentId.generate();
        UserAgentId id2 = UserAgentId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }
}
