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
 * - UserAgent 토큰 발급 (issueToken) 테스트
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

    @Test
    void shouldIssueToken() {
        // Given
        UserAgent userAgent = UserAgent.create("Mozilla/5.0 (Windows NT 10.0; Win64; x64)...");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        // When
        userAgent.issueToken(token);

        // Then
        assertThat(userAgent.getToken()).isEqualTo(token);
        assertThat(userAgent.getTokenIssuedAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenTokenIsBlank() {
        // Given
        UserAgent userAgent = UserAgent.create("Mozilla/5.0 (Windows NT 10.0; Win64; x64)...");

        // When & Then
        assertThatThrownBy(() -> userAgent.issueToken(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("토큰은 비어있을 수 없습니다");
    }
}
