package com.ryuqq.crawlinghub.application.useragent.port.out.session;

import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import java.util.Optional;

/**
 * 세션 토큰 발급 Port
 *
 * <p>외부 사이트에 접속하여 세션 토큰을 발급받는 Port입니다.
 *
 * <p><strong>동작 흐름</strong>:
 *
 * <ol>
 *   <li>타겟 URL에 User-Agent 헤더 포함하여 HTTP 요청
 *   <li>Response Cookie에서 세션 토큰 추출
 *   <li>Response Header에서 만료 시간 파싱
 * </ol>
 *
 * <p><strong>구현 주의사항</strong>:
 *
 * <ul>
 *   <li>HTTP Client 타임아웃 설정 필수
 *   <li>네트워크 오류 시 Optional.empty() 반환
 *   <li>Cookie 파싱 실패 시 Optional.empty() 반환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SessionTokenPort {

    /**
     * 세션 토큰 발급
     *
     * <p>타겟 URL에 User-Agent 헤더를 포함하여 접속하고, Response Cookie에서 세션 토큰을 추출합니다.
     *
     * @param userAgentValue User-Agent 헤더 값
     * @return 발급된 세션 토큰 (실패 시 empty)
     */
    Optional<SessionToken> issueSessionToken(String userAgentValue);
}
