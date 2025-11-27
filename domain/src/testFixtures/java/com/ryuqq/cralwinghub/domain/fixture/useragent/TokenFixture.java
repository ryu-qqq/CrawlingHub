package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.Token;

/**
 * Token Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class TokenFixture {

    // 유효한 Base64 토큰 (최소 44자)
    private static final String DEFAULT_TOKEN =
            "dGhpc0lzQVZhbGlkQmFzZTY0VG9rZW5Gb3JUZXN0aW5nUHVycG9zZXM=";
    private static final String ALTERNATIVE_TOKEN =
            "YW5vdGhlclZhbGlkQmFzZTY0VG9rZW5Gb3JUZXN0aW5nT25seQ==";

    /**
     * 기본 토큰 생성
     *
     * @return Token
     */
    public static Token aDefaultToken() {
        return Token.of(DEFAULT_TOKEN);
    }

    /**
     * 대체 토큰 생성
     *
     * @return Token
     */
    public static Token anAlternativeToken() {
        return Token.of(ALTERNATIVE_TOKEN);
    }

    /**
     * 특정 값으로 토큰 생성
     *
     * @param encryptedValue 암호화된 토큰 값
     * @return Token
     */
    public static Token aToken(String encryptedValue) {
        return Token.of(encryptedValue);
    }

    private TokenFixture() {
        // Utility class
    }
}
