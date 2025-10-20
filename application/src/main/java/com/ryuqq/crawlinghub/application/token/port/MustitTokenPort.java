package com.ryuqq.crawlinghub.application.token.port;

import com.ryuqq.crawlinghub.domain.token.TokenResponse;

/**
 * 머스트잇 API 토큰 관리 Port (Outbound)
 * 외부 머스트잇 API와의 통신을 추상화
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
public interface MustitTokenPort {

    /**
     * 토큰 발급
     *
     * @param userAgent 토큰을 발급받을 User-Agent
     * @return 발급된 토큰 정보
     * @throws com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException 토큰 발급 실패 시
     */
    TokenResponse issueToken(String userAgent);

    /**
     * 토큰 갱신
     *
     * @param refreshToken 갱신용 토큰
     * @return 갱신된 토큰 정보
     * @throws com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException 토큰 갱신 실패 시
     */
    TokenResponse refreshToken(String refreshToken);

    /**
     * JWT 형식 검증 (구조적 검증만 수행)
     *
     * 주의: 이 메서드는 토큰의 서명, 만료시간, 발급자 등을 검증하지 않습니다.
     * JWT의 구조(header.payload.signature)와 Base64 URL-safe 인코딩만 확인합니다.
     * 실제 토큰의 유효성은 Mustit API 호출 시 검증되며,
     * 만료 여부는 데이터베이스에 저장된 issuedAt과 expiresIn을 통해 확인해야 합니다.
     *
     * @param accessToken 검증할 액세스 토큰
     * @return JWT 형식이면 true, 그렇지 않으면 false
     */
    boolean isJwtFormat(String accessToken);
}
