package com.ryuqq.crawlinghub.application.token.port;

import com.ryuqq.crawlinghub.domain.token.Token;

/**
 * 외부 API 토큰 발급 Port
 * <p>
 * ⭐ Domain 타입(Token)을 반환
 * - 외부 MustIt API와의 통신을 추상화
 * - Adapter 계층이 실제 HTTP 통신 구현
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface MustItTokenPort {

    /**
     * MustIt API에서 토큰 발급
     *
     * @param userAgentString User-Agent 문자열
     * @return Token Domain 객체
     */
    Token issueToken(String userAgentString);
}
