package com.ryuqq.crawlinghub.application.legacy.token.port;

import java.util.List;

/**
 * User-Agent Token Port (Outbound)
 * DB User-Agent 및 Token 관리
 *
 * @author crawlinghub
 * @since 2025-10-14
 */
public interface UserAgentTokenPort {

    /**
     * 활성 토큰 조회
     */
    UserAgentInfo findActiveToken(Long userAgentId);

    /**
     * 토큰 저장 또는 업데이트
     *
     * @param userAgentId User-Agent ID
     * @param tokenValue 토큰 값
     * @param tokenType 토큰 타입 (Bearer)
     * @param expiresIn 만료 시간 (초)
     */
    void saveOrUpdateToken(Long userAgentId, String tokenValue, String tokenType, long expiresIn);

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
