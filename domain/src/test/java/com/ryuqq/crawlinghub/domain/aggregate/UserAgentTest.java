package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.fixture.UserAgentFixture;
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
 * - UserAgent 토큰 버킷 리미터 (canMakeRequest) 테스트 - Tell Don't Ask 패턴
 * - UserAgent 상태 전환 (suspend, activate) 테스트
 * - 리팩토링: 정적 팩토리 메서드 패턴 (forNew/of/reconstitute) 테스트
 */
class UserAgentTest {

    // ========== 리팩토링: 정적 팩토리 메서드 패턴 테스트 ==========

    @Test
    void shouldCreateUserAgentUsingForNew() {
        // Given
        String userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)";

        // When
        UserAgent userAgent = UserAgent.forNew(userAgentString);

        // Then
        assertThat(userAgent.getUserAgentId()).isNotNull();
        assertThat(userAgent.getUserAgentString()).isEqualTo(userAgentString);
        assertThat(userAgent.getToken()).isNull();
        assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.ACTIVE);
        assertThat(userAgent.getRequestCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateUserAgentUsingOf() {
        // Given
        String userAgentString = "Mozilla/5.0 (Linux; Android 10)";

        // When
        UserAgent userAgent = UserAgent.of(userAgentString);

        // Then
        assertThat(userAgent.getUserAgentId()).isNotNull();
        assertThat(userAgent.getUserAgentString()).isEqualTo(userAgentString);
        assertThat(userAgent.getToken()).isNull();
        assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.ACTIVE);
    }

    @Test
    void shouldReconstituteUserAgentWithAllFields() {
        // Given
        UserAgentId userAgentId = UserAgentId.generate();
        String userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)";
        String token = "test_token_reconstitute";
        UserAgentStatus status = UserAgentStatus.SUSPENDED;
        Integer requestCount = 50;

        // When
        UserAgent userAgent = UserAgent.reconstitute(userAgentId, userAgentString, token, status, requestCount);

        // Then
        assertThat(userAgent.getUserAgentId()).isEqualTo(userAgentId);
        assertThat(userAgent.getUserAgentString()).isEqualTo(userAgentString);
        assertThat(userAgent.getToken()).isEqualTo(token);
        assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);
        assertThat(userAgent.getRequestCount()).isEqualTo(50);
    }

    // ========== 기존 테스트 (레거시, 유지보수용) ==========

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

    @Test
    void shouldAllowRequestWhenUnder80RequestsPerHour() {
        // Given - 토큰 발급된 UserAgent
        UserAgent userAgent = UserAgent.create("Mozilla/5.0 (Windows NT 10.0; Win64; x64)...");
        userAgent.issueToken("test_token_123");

        // When - 79번 요청 실행 (80번째 요청 가능 확인)
        for (int i = 0; i < 79; i++) {
            userAgent.incrementRequestCount();
        }

        // Then
        assertThat(userAgent.canMakeRequest()).isTrue();
    }

    @Test
    void shouldBlockRequestWhen80RequestsReachedInSameHour() {
        // Given - 토큰 발급된 UserAgent
        UserAgent userAgent = UserAgent.create("Mozilla/5.0 (Windows NT 10.0; Win64; x64)...");
        userAgent.issueToken("test_token_123");

        // When - 80번 요청 실행 (81번째 요청 차단 확인)
        for (int i = 0; i < 80; i++) {
            userAgent.incrementRequestCount();
        }

        // Then
        assertThat(userAgent.canMakeRequest()).isFalse();
    }

    @Test
    void shouldNotAllowRequestWhenTokenIsNull() {
        // Given - 토큰 없는 UserAgent
        UserAgent userAgent = UserAgent.create("Mozilla/5.0 (Windows NT 10.0; Win64; x64)...");

        // When & Then
        assertThat(userAgent.canMakeRequest()).isFalse();
    }

    @Test
    void shouldSuspendUserAgentOn429Response() {
        // Given - 토큰이 발급된 ACTIVE 상태의 UserAgent
        UserAgent userAgent = UserAgentFixture.userAgentWithToken();

        // When - 429 Too Many Requests 응답으로 일시 정지
        userAgent.suspend();

        // Then
        assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);
    }

    @Test
    void shouldActivateUserAgent() {
        // Given - SUSPENDED 상태의 UserAgent
        UserAgent userAgent = UserAgentFixture.userAgentWithToken();
        userAgent.suspend();

        // When - 다시 활성화
        userAgent.activate();

        // Then
        assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.ACTIVE);
    }
}
