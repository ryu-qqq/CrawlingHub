package com.ryuqq.crawlinghub.application.token.port;

import com.ryuqq.crawlinghub.domain.useragent.UserAgent;

/**
 * User-Agent Pool 관리 Port
 * <p>
 * ⭐ Domain 타입(UserAgent)을 사용
 * - Redis 기반 LRU Pool 관리를 추상화
 * - Adapter 계층이 Redis 구현 제공
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface UserAgentPoolPort {

    /**
     * LRU 방식으로 User-Agent 선택
     * <p>
     * Redis Sorted Set에서 score가 가장 작은 User-Agent를 선택하고 Pool에서 제거
     * </p>
     *
     * @return UserAgent Domain 객체 (토큰 없는 상태)
     */
    UserAgent acquireLeastRecentlyUsed();

    /**
     * Pool로 반환
     * <p>
     * 현재 타임스탬프를 score로 하여 Redis Sorted Set에 다시 추가
     * </p>
     *
     * @param userAgent UserAgent Domain 객체
     */
    void returnToPool(UserAgent userAgent);

    /**
     * Pool에서 제거
     * <p>
     * User-Agent가 DISABLED 상태일 때 호출
     * </p>
     *
     * @param userAgent UserAgent Domain 객체
     */
    void removeFromPool(UserAgent userAgent);
}
