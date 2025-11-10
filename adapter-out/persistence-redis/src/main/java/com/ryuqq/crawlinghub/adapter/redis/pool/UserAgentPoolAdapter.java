package com.ryuqq.crawlinghub.adapter.redis.pool;

import com.ryuqq.crawlinghub.application.token.port.UserAgentPoolPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;
import org.springframework.stereotype.Component;

/**
 * User-Agent Pool Adapter
 * <p>
 * ⭐ Domain 중심 설계:
 * - UserAgent Domain 객체 사용
 * - UserAgentPoolManager(Long 기반)를 Domain으로 변환
 * </p>
 * <p>
 * 역할:
 * - Redis Sorted Set 기반 LRU Pool 관리
 * - Long ID ↔ UserAgent Domain 변환
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class UserAgentPoolAdapter implements UserAgentPoolPort {

    private final UserAgentPoolManager poolManager;

    public UserAgentPoolAdapter(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    /**
     * LRU 방식으로 User-Agent 선택
     * <p>
     * Redis Pool에서 Long ID를 가져와 UserAgent Domain 객체로 변환
     * ⚠️ 주의: 이 시점에서는 ID만 있음 (토큰 정보는 DB에서 로드 필요)
     * </p>
     *
     * @return UserAgent Domain 객체 (ID만 설정, 나머지는 null)
     */
    @Override
    public UserAgent acquireLeastRecentlyUsed() {
        Long userAgentId = poolManager.acquireLeastRecentlyUsed();

        if (userAgentId == null) {
            return null;
        }

        // ID만으로 Domain 객체 생성 (토큰 정보는 DB에서 로드 필요)
        return UserAgent.reconstitute(
            UserAgentId.of(userAgentId),
            "temp",  // userAgentString - DB에서 로드 필요 (임시값)
            null,    // currentToken (Token VO) - DB에서 로드 필요
            null,    // tokenStatus - DB에서 로드 필요
            null,    // remainingRequests - DB에서 로드 필요
            null,    // rateLimitResetAt - DB에서 로드 필요
            null,    // createdAt - DB에서 로드 필요
            null     // updatedAt - DB에서 로드 필요
        );
    }

    /**
     * Pool로 반환
     * <p>
     * UserAgent Domain 객체의 ID를 추출하여 Redis Pool에 반환
     * </p>
     *
     * @param userAgent UserAgent Domain 객체
     */
    @Override
    public void returnToPool(UserAgent userAgent) {
        if (userAgent == null || userAgent.getIdValue() == null) {
            throw new IllegalArgumentException("userAgent and userAgent.id must not be null");
        }

        poolManager.returnToPool(userAgent.getIdValue());
    }

    /**
     * Pool에서 제거
     * <p>
     * UserAgent Domain 객체의 ID를 추출하여 Redis Pool에서 제거
     * </p>
     *
     * @param userAgent UserAgent Domain 객체
     */
    @Override
    public void removeFromPool(UserAgent userAgent) {
        if (userAgent == null || userAgent.getIdValue() == null) {
            throw new IllegalArgumentException("userAgent and userAgent.id must not be null");
        }

        poolManager.removeFromPool(userAgent.getIdValue());
    }
}
