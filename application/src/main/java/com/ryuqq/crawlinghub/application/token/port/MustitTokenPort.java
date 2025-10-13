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
     * 토큰 유효성 검증
     *
     * @param accessToken 검증할 액세스 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    boolean validateToken(String accessToken);
}
