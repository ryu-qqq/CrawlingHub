package com.ryuqq.crawlinghub.application.token.port;

/**
 * Rate Limiter Port
 * <p>
 * - Redis 기반 Token Bucket Rate Limiting
 * - 시간당 80회 요청 제한
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface RateLimiterPort {

    /**
     * 요청 소비 시도
     *
     * @param userAgentId User-Agent ID
     * @return 소비 성공 여부
     */
    boolean tryConsume(Long userAgentId);

    /**
     * 대기 시간 조회
     *
     * @param userAgentId User-Agent ID
     * @param tokensRequired 필요한 토큰 수
     * @return 대기 시간 (밀리초)
     */
    long getWaitTime(Long userAgentId, int tokensRequired);
}
