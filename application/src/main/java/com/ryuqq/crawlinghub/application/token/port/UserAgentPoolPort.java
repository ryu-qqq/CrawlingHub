package com.ryuqq.crawlinghub.application.token.port;

/**
 * User-Agent Pool Port (Outbound)
 * Redis Pool 관리
 *
 * @author crawlinghub
 */
public interface UserAgentPoolPort {

    /**
     * Pool에 User-Agent 추가
     */
    void addToPool(Long userAgentId);

    /**
     * LRU User-Agent 획득
     */
    Long acquireLeastRecentlyUsed();

    /**
     * Pool에 반환
     */
    void returnToPool(Long userAgentId);

    /**
     * Pool 초기화
     */
    void clearPool();
}
