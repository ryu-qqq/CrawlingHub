package com.ryuqq.crawlinghub.adapter.mustit;

import com.ryuqq.crawlinghub.domain.token.TokenResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MustitTokenAdapter 통합 테스트
 * 실제 Mustit 웹사이트에 접속하여 토큰 발급을 테스트합니다.
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 * @since 2025-10-14
 */
class MustitTokenAdapterIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(MustitTokenAdapterIntegrationTest.class);

    private final MustitTokenAdapter adapter = new MustitTokenAdapter(
            WebClient.builder().build()
    );

    @Test
    @DisplayName("iOS Safari User-Agent로 토큰 발급 성공")
    void shouldIssueTokenWithIOSUserAgent() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

        // when
        TokenResponse response = adapter.issueToken(userAgent);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.refreshToken()).isNull();
        assertThat(response.expiresIn()).isGreaterThan(0);
        assertThat(response.issuedAt()).isNotNull();

        LOG.info("Token issued successfully:");
        LOG.info("  - Token length: {} bytes", response.accessToken().length());
        LOG.info("  - Token type: {}", response.tokenType());
        LOG.info("  - Expires in: {}s", response.expiresIn());
        LOG.info("  - Is expired: {}", response.isExpired());
    }

    @Test
    @DisplayName("Android Chrome User-Agent로 토큰 발급 성공")
    void shouldIssueTokenWithAndroidUserAgent() {
        // given
        String userAgent = "Mozilla/5.0 (Linux; Android 13; SM-S908B) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36";

        // when
        TokenResponse response = adapter.issueToken(userAgent);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.refreshToken()).isNull();
    }

    @Test
    @DisplayName("발급된 토큰이 JWT 형식인지 검증")
    void shouldValidateIssuedTokenAsJWT() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

        // when
        TokenResponse response = adapter.issueToken(userAgent);

        // then
        String[] parts = response.accessToken().split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).matches("[A-Za-z0-9_-]+"); // Header
        assertThat(parts[1]).matches("[A-Za-z0-9_-]+"); // Payload
        assertThat(parts[2]).matches("[A-Za-z0-9_-]+"); // Signature

        LOG.info("JWT Structure validated:");
        LOG.info("  - Header length: {}", parts[0].length());
        LOG.info("  - Payload length: {}", parts[1].length());
        LOG.info("  - Signature length: {}", parts[2].length());
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    void shouldValidateValidToken() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        TokenResponse response = adapter.issueToken(userAgent);

        // when
        boolean isValid = adapter.validateToken(response.accessToken());

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 잘못된 형식")
    void shouldRejectInvalidTokenFormat() {
        // given
        String invalidToken = "not.a.valid.jwt.token";

        // when
        boolean isValid = adapter.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - null 토큰")
    void shouldRejectNullToken() {
        // when
        boolean isValid = adapter.validateToken(null);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 빈 문자열")
    void shouldRejectEmptyToken() {
        // when
        boolean isValid = adapter.validateToken("");

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("refresh token은 지원하지 않음")
    void shouldThrowExceptionWhenRefreshingToken() {
        // when & then
        assertThatThrownBy(() -> adapter.refreshToken("dummy-refresh-token"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Mustit does not provide refresh token");
    }

    @Test
    @DisplayName("토큰 만료 시간이 올바르게 계산됨")
    void shouldCalculateExpirationTimeCorrectly() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

        // when
        TokenResponse response = adapter.issueToken(userAgent);

        // then
        assertThat(response.getExpirationTime())
                .isAfter(response.issuedAt())
                .isEqualTo(response.issuedAt().plusSeconds(response.expiresIn()));

        assertThat(response.isExpired()).isFalse();
        assertThat(response.needsRefresh()).isFalse();

        LOG.info("Expiration info:");
        LOG.info("  - Issued at: {}", response.issuedAt());
        LOG.info("  - Expires at: {}", response.getExpirationTime());
        LOG.info("  - Is expired: {}", response.isExpired());
        LOG.info("  - Needs refresh: {}", response.needsRefresh());
    }

    @Test
    @DisplayName("연속으로 토큰 발급 시 각각 다른 토큰 발급됨")
    void shouldIssueDistinctTokensForSequentialRequests() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

        // when
        TokenResponse first = adapter.issueToken(userAgent);
        TokenResponse second = adapter.issueToken(userAgent);

        // then
        assertThat(first.accessToken()).isNotEqualTo(second.accessToken());
        LOG.info("Two distinct tokens issued successfully");
    }
}
