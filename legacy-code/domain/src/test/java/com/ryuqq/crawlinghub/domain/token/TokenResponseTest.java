package com.ryuqq.crawlinghub.domain.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenResponse 도메인 모델 테스트
 *
 * @author crawlinghub
 */
class TokenResponseTest {

    @Test
    @DisplayName("TokenResponse 생성 성공")
    void createTokenResponse() {
        // when
        TokenResponse response = TokenResponse.of(
                "access-token",
                "refresh-token",
                3600L,
                "Bearer"
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.issuedAt()).isNotNull();
    }

    @Test
    @DisplayName("토큰 만료 시간 계산")
    void getExpirationTime() {
        // given
        long expiresIn = 3600L;
        TokenResponse response = TokenResponse.of(
                "access-token",
                "refresh-token",
                expiresIn,
                "Bearer"
        );

        // when
        Instant expirationTime = response.getExpirationTime();

        // then
        assertThat(expirationTime).isNotNull();
        assertThat(expirationTime).isAfter(response.issuedAt());
        assertThat(expirationTime).isEqualTo(response.issuedAt().plusSeconds(expiresIn));
    }

    @Test
    @DisplayName("토큰 만료 여부 확인 - 만료되지 않음")
    void isExpired_NotExpired() {
        // given
        TokenResponse response = TokenResponse.of(
                "access-token",
                "refresh-token",
                3600L,
                "Bearer"
        );

        // when
        boolean expired = response.isExpired();

        // then
        assertThat(expired).isFalse();
    }

    @Test
    @DisplayName("토큰 만료 여부 확인 - 만료됨")
    void isExpired_Expired() {
        // given
        TokenResponse response = new TokenResponse(
                "access-token",
                "refresh-token",
                1L,
                "Bearer",
                Instant.now().minusSeconds(10)
        );

        // when
        boolean expired = response.isExpired();

        // then
        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("토큰 갱신 필요 여부 - 필요하지 않음")
    void needsRefresh_NotNeeded() {
        // given
        TokenResponse response = TokenResponse.of(
                "access-token",
                "refresh-token",
                3600L,
                "Bearer"
        );

        // when
        boolean needsRefresh = response.needsRefresh();

        // then
        assertThat(needsRefresh).isFalse();
    }

    @Test
    @DisplayName("토큰 갱신 필요 여부 - 필요함 (만료 5분 전)")
    void needsRefresh_Needed() {
        // given
        TokenResponse response = new TokenResponse(
                "access-token",
                "refresh-token",
                300L, // 5분
                "Bearer",
                Instant.now().minusSeconds(10)
        );

        // when
        boolean needsRefresh = response.needsRefresh();

        // then
        assertThat(needsRefresh).isTrue();
    }
}
