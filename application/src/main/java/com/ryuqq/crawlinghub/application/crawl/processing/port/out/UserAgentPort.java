package com.ryuqq.crawlinghub.application.crawl.processing.port.out;

/**
 * User-Agent 관리 Port
 *
 * <p>크롤링 시 사용할 User-Agent를 선택하고 관리합니다.
 * 탐지 방지를 위해 User-Agent를 로테이션합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface UserAgentPort {

    /**
     * User-Agent 선택 (로테이션)
     *
     * @return User-Agent 문자열
     */
    String selectUserAgent();

    /**
     * User-Agent 블랙리스트 등록
     *
     * @param userAgent 블랙리스트에 추가할 User-Agent
     */
    void blacklist(String userAgent);
}
