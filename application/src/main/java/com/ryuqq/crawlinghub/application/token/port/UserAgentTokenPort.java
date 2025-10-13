package com.ryuqq.crawlinghub.application.token.port;

import java.util.List;

/**
 * User-Agent Token Port (Outbound)
 * DB User-Agent 및 Token 관리
 *
 * @author crawlinghub
 */
public interface UserAgentTokenPort {

    /**
     * 활성 토큰 조회
     */
    UserAgentInfo findActiveToken(Long userAgentId);

    /**
     * 사용 기록
     */
    void recordUsage(Long userAgentId);

    /**
     * 성공 기록
     */
    void recordSuccess(Long userAgentId);

    /**
     * 실패 기록
     */
    void recordFailure(Long userAgentId);

    /**
     * 활성 User-Agent ID 목록 조회
     */
    List<Long> findAllActiveUserAgents();
}
