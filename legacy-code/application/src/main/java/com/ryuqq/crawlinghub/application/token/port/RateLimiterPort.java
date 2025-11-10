package com.ryuqq.crawlinghub.application.legacy.token.port;

/**
 * Rate Limiter Port (Outbound)
 *
 * @author crawlinghub
 */
public interface RateLimiterPort {

    /**
     * Token 소비 시도
     *
     * @param userAgentId User-Agent ID
     * @return 허용 여부
     */
    boolean tryConsume(Long userAgentId);

    /**
     * Token Bucket 초기화
     *
     * @param userAgentId User-Agent ID
     */
    void initialize(Long userAgentId);
}
