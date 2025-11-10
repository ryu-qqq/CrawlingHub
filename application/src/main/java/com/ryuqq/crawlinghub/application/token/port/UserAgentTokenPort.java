package com.ryuqq.crawlinghub.application.token.port;

import com.ryuqq.crawlinghub.domain.useragent.UserAgent;

/**
 * User-Agent 토큰 관리 Port
 * <p>
 * ⭐ Domain 타입(UserAgent)을 사용
 * - Hexagonal Architecture의 Port 인터페이스
 * - Adapter 계층이 이 인터페이스를 구현
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface UserAgentTokenPort {

    /**
     * User-Agent ID로 조회
     *
     * @param userAgentId User-Agent ID
     * @return UserAgent Domain 객체
     */
    UserAgent findById(Long userAgentId);

    /**
     * User-Agent 저장 (토큰 포함)
     *
     * @param userAgent UserAgent Domain 객체
     */
    void save(UserAgent userAgent);

    /**
     * 사용 통계 기록
     *
     * @param userAgent UserAgent Domain 객체
     */
    void recordUsage(UserAgent userAgent);
}
