package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.UserAgentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserAgent Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - UserAgent 생성 (create) 테스트
 */
class UserAgentTest {

    @Test
    void shouldCreateUserAgentWithActiveStatus() {
        // Given
        String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...";

        // When
        UserAgent userAgent = UserAgent.create(userAgentString);

        // Then
        assertThat(userAgent.getUserAgentId()).isNotNull();
        assertThat(userAgent.getUserAgentString()).isEqualTo(userAgentString);
        assertThat(userAgent.getToken()).isNull();
        assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.ACTIVE);
        assertThat(userAgent.getRequestCount()).isEqualTo(0);
    }

    @Test
    void shouldThrowExceptionWhenUserAgentStringIsBlank() {
        // When & Then
        assertThatThrownBy(() -> UserAgent.create(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserAgent 문자열은 비어있을 수 없습니다");
    }
}
